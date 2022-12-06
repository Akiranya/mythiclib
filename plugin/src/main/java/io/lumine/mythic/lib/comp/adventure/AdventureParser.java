package io.lumine.mythic.lib.comp.adventure;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgument;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.resolver.ContextTagResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;
import io.lumine.mythic.lib.comp.adventure.tag.implementation.*;
import io.lumine.mythic.lib.comp.adventure.tag.implementation.decorations.*;
import io.lumine.mythic.lib.util.AdventureUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
@ApiStatus.NonExtendable
public class AdventureParser {

    /* Constants */
    private static final Pattern TAG_REGEX = Pattern.compile("(?i)(?<=<).*?(?=>)");

    private final List<AdventureTag> tags = new ArrayList<>();
    private final Function<String, String> fallBackResolver;

    public AdventureParser(@NotNull Function<String, String> fallBackResolver) {
        this.fallBackResolver = fallBackResolver;
    }

    public AdventureParser() {
        this(s -> "<invalid>");

        // Context
        add(new GradientTag());
        add(new RainbowTag());
        add(new TransitionTag());

        // Normal
        add(new VanillaColorTag());
        add(new HexColorTag());
        add(new AdventureColorTag());
        add(new NewlineTag());
        add(new BoldTag());
        add(new ItalicTag());
        add(new ObfuscatedTag());
        add(new ResetTag());
        add(new StrikethroughTag());
        add(new UnderlineTag());
    }

    /**
     * Parse a string synchronously.
     * Note that if a tag is not found or invalid, it will be replaced by the fallback resolver.
     * Also note that this method will not parse
     * the <newline> tag, you will have to use {@link #parse(Collection)} or {@link #parseAsync(Collection)}.
     *
     * @param src The string to parse.
     * @return The parsed string.
     */
    public @NotNull String parse(@NotNull final String src) {
        String cpy = src;
        Matcher matcher = TAG_REGEX.matcher(cpy);
        while (matcher.find()) {
            final String tag = matcher.group();
            final String tagName = tag.contains(":") ? tag.split(":")[0] : tag;
            final String finalCpy = cpy;
            if (tagName.isEmpty() || tagName.startsWith("/"))
                continue;

            cpy = findByName(tagName)
                    .map(adventureTag -> parseTag(finalCpy, adventureTag, tagName, tag))
                    .orElseGet(() -> {
                        // Hex color
                        if ((tagName.length() == 7 && tagName.startsWith("#"))
                                || (tagName.length() == 9 && tagName.startsWith("HEX"))) {
                            final String prefix = tagName.startsWith("#") ? "#" : "HEX";
                            final String hex = tagName.substring(tagName.startsWith("#") ? 1 : 3);
                            if (hex.matches("[0-9a-fA-F]+"))
                                return findByName("#")
                                        .map(adventureTag -> parseTag(finalCpy, adventureTag, prefix, tag))
                                        .orElse(finalCpy.replace("<" + tag + ">", fallBackResolver.apply(tag)));
                        }

                        // Fall back
                        return finalCpy.replace("<" + tag + ">", fallBackResolver.apply(tag));
                    });
        }
        cpy = removeUnparsedAndUselessTags(cpy);
        return minecraftColorization(cpy);
    }

    /**
     * Parse a string asynchronously.
     *
     * @param src The string to parse.
     * @return A {@link CompletableFuture} containing the parsed string.
     */
    public @NotNull CompletableFuture<String> parseAsync(@NotNull final String src) {
        return AdventureUtils.supplyAsync(() -> parse(src));
    }

    /**
     * Parse a collection of strings synchronously.
     *
     * @param src The collection of strings to parse.
     * @return The parsed collection of strings.
     */
    public @NotNull Collection<String> parse(@NotNull final Collection<String> src) {
        List<String> parsed = src.stream()
                .map(this::parse)
                .collect(Collectors.toList());
        List<String> finalList = new ArrayList<>();
        for (final String line : parsed) {
            if (!line.contains("\n")) {
                finalList.add(line);
                continue;
            }
            finalList.addAll(Arrays.asList(line.split("\n")));
        }
        return finalList;
    }

    /**
     * Parse a collection of strings asynchronously.
     *
     * @param src The collection of strings to parse.
     * @return A future containing the parsed collection.
     */
    public @NotNull CompletableFuture<Collection<String>> parseAsync(@NotNull Collection<String> src) {
        return AdventureUtils.supplyAsync(() -> parse(src));
    }

    /**
     * Parse a tag and replace it in the string.
     * Note that if the tag is not found or invalid, it will be replaced by the fallback resolver.
     *
     * @param src           The source string.
     * @param tag           The tag to parse.
     * @param tagIdentifier The tag identifier.
     * @param plainTag      The plain tag.
     * @return The parsed string.
     */
    private @NotNull String parseTag(@NotNull final String src, @NotNull final AdventureTag tag, @NotNull final String tagIdentifier, @NotNull final String plainTag) {
        String cpy = src;
        try {
            int firstArgIndex = plainTag.indexOf(":");
            boolean hasArgs = firstArgIndex != -1;
            boolean isHex = tagIdentifier.equals("#") || tagIdentifier.equalsIgnoreCase("HEX");
            String hexPrefix = tagIdentifier.startsWith("#") ? "#" : "HEX";

            final String rawTag = isHex ? tagIdentifier : plainTag.substring(0, hasArgs ? firstArgIndex : plainTag.length());
            final String rawArgs = isHex ? plainTag.substring(hexPrefix.length()) : (hasArgs ? plainTag.substring(firstArgIndex + 1) : "");
            final String original = "<%s%s>".formatted(rawTag, hasArgs ? ':' + rawArgs : rawArgs);

            final AdventureArgumentQueue args = parseArguments(rawArgs);
            boolean hasContext = tag.resolver() instanceof ContextTagResolver;

            String context = hasContext ? getTagContent(cpy, rawTag, original) : null;
            String resolved = hasContext ?
                    ((ContextTagResolver) tag.resolver()).resolve(rawTag, args, context)
                    : tag.resolver().resolve(rawTag, args);
            cpy = cpy.replace(hasContext ?
                    Objects.requireNonNullElse("%s%s".formatted(original, context), fallBackResolver.apply(original))
                    : original, Objects.requireNonNullElse(resolved, fallBackResolver.apply(original)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cpy;
    }

    /**
     * Parse the tag context, which is basically everything between the tag and the closing tag.
     * or the end of the string if there is no closing tag.
     *
     * @param src           The source string.
     * @param tagName       The tag name.
     * @param tagIdentifier The tag identifier.
     * @return The tag context.
     */
    private @NotNull String getTagContent(@NotNull String src, @NotNull String tagName, @NotNull String tagIdentifier) {
        // Match closed tags
        final String closeTag = "</%s>".formatted(tagName);
        String content = StringUtils.substringBetween(src, tagIdentifier, closeTag);
        if (content != null) return content;

        String cpy = src.substring(src.indexOf(tagIdentifier) + tagIdentifier.length());
        int colorIndex = cpy.length();

        // Match colors tags as context end
        Matcher matcher = TAG_REGEX.matcher(cpy);
        int iterations = 0;
        while (matcher.find() && iterations++ < 10) {
            final String rawTag = matcher.group();
            String[] split = rawTag.split(":");
            Optional<AdventureTag> optTag = findByName(split[0]).filter(AdventureTag::color);
            if (optTag.isPresent()) {
                colorIndex = cpy.indexOf("<%s>".formatted(rawTag));
                break;
            }
            matcher = TAG_REGEX.matcher(cpy);
        }
        content = cpy.substring(0, colorIndex);
        return content;
    }

    /**
     * Parse tags arguments from a string.
     *
     * @param rawArgs The raw arguments.
     * @return The parsed arguments as {@link AdventureArgumentQueue}.
     */
    private AdventureArgumentQueue parseArguments(@NotNull String rawArgs) {
        String[] unparsedArgs = rawArgs.split(":");
        if (unparsedArgs.length > 0 && unparsedArgs[0].isEmpty())
            unparsedArgs = Arrays.copyOfRange(unparsedArgs, 1, unparsedArgs.length);
        return new AdventureArgumentQueue(
                Arrays.stream(unparsedArgs)
                        .map(AdventureArgument::new)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Remove all unparsed tags and close tags.
     * Note: close tags are replaced with reset character.
     *
     * @param src The source string.
     * @return The parsed string.
     */
    private @NotNull String removeUnparsedAndUselessTags(@NotNull String src) {
        Matcher matcher = TAG_REGEX.matcher(src);
        int iterations = 0;
        while (matcher.find() && iterations++ < 50) {
            final String matched = matcher.group();
            final String original = "<%s>".formatted(matched);
            src = src.replace(original, matched.startsWith("/") ? "§r" : fallBackResolver.apply(original));
            matcher = TAG_REGEX.matcher(src);
        }
        return src;
    }

    /**
     * Minecraft colorization is a feature that allows to use the § character to colorize text.
     *
     * @param src The source string to colorize.
     * @return The colorized string.
     */
    private @NotNull String minecraftColorization(@NotNull final String src) {
        return ChatColor.translateAlternateColorCodes('&', src);
    }

    /**
     * Register a new tag and check if it's compatible with the server
     * current version.
     *
     * @param tag The tag to register.
     */
    public void add(AdventureTag tag) {
        if (tag.backwardsCompatible() && MythicLib.plugin.getVersion().isBelowOrEqual(1, 15)) {
            MythicLib.plugin.getLogger().warning("The tag %s is not compatible with your server version.".formatted(tag.name()));
            return;
        }
        tags.add(tag);
    }

    /**
     * Force register a tag, without checking for compatibility.
     *
     * @param tag The tag to register.
     */
    @ApiStatus.Internal
    public void forceRegister(AdventureTag tag) {
        tags.add(tag);
    }

    /**
     * Remove a registered tag.
     *
     * @param tag The tag to remove
     */
    public void remove(AdventureTag tag) {
        tags.remove(tag);
    }

    /**
     * This method will return an optional containing the tag if it exists.
     *
     * @param name The name of the tag to find.
     * @return An optional containing the tag if it exists.
     */
    public Optional<AdventureTag> findByName(@NotNull String name) {
        return tags.stream()
                .filter(tag -> tag.name().equalsIgnoreCase(name) || tag.aliases().stream().anyMatch(s -> s.equalsIgnoreCase(name)))
                .findFirst();
    }

    /**
     * Get the list of registered tags.
     *
     * @return A list of all registered tags
     */
    public List<AdventureTag> tags() {
        return tags;
    }
}
