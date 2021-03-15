package io.lumine.mythic.lib.api.util;

import io.lumine.utils.adventure.key.Key;
import io.lumine.utils.adventure.text.Component;
import io.lumine.utils.adventure.text.KeybindComponent;
import io.lumine.utils.adventure.text.TextComponent;
import io.lumine.utils.adventure.text.TranslatableComponent;
import io.lumine.utils.adventure.text.event.ClickEvent;
import io.lumine.utils.adventure.text.event.HoverEvent;
import io.lumine.utils.adventure.text.format.NamedTextColor;
import io.lumine.utils.adventure.text.format.TextColor;
import io.lumine.utils.adventure.text.format.TextDecoration;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.lumine.utils.adventure.text.minimessage.Tokens.BOLD;
import static io.lumine.utils.adventure.text.minimessage.Tokens.CLICK;
import static io.lumine.utils.adventure.text.minimessage.Tokens.CLOSE_TAG;
import static io.lumine.utils.adventure.text.minimessage.Tokens.COLOR;
import static io.lumine.utils.adventure.text.minimessage.Tokens.FONT;
import static io.lumine.utils.adventure.text.minimessage.Tokens.HOVER;
import static io.lumine.utils.adventure.text.minimessage.Tokens.INSERTION;
import static io.lumine.utils.adventure.text.minimessage.Tokens.ITALIC;
import static io.lumine.utils.adventure.text.minimessage.Tokens.KEYBIND;
import static io.lumine.utils.adventure.text.minimessage.Tokens.OBFUSCATED;
import static io.lumine.utils.adventure.text.minimessage.Tokens.SEPARATOR;
import static io.lumine.utils.adventure.text.minimessage.Tokens.STRIKETHROUGH;
import static io.lumine.utils.adventure.text.minimessage.Tokens.TAG_END;
import static io.lumine.utils.adventure.text.minimessage.Tokens.TAG_START;
import static io.lumine.utils.adventure.text.minimessage.Tokens.TRANSLATABLE;
import static io.lumine.utils.adventure.text.minimessage.Tokens.UNDERLINED;

public class MiniMessageSerializer {

    private MiniMessageSerializer() {
    }

    public static @NonNull String serialize(final @NonNull Component component) {
        final StringBuilder sb = new StringBuilder();

        final List<Component> components = new ArrayList<>();
        components.add(component);

        for(int i = 0; i < components.size(); i++) {
            final Component comp = components.get(i);

            // add childs
            components.addAll(i + 1, comp.children());

            // # start tags

            // ## get prev comp
            Component prevComp = null;
            if(i > 0) {
                prevComp = components.get(i - 1);
            }

            // ## color
            // ### white is not important
            if(comp.color() != null && (prevComp == null || prevComp.color() != comp.color())) {
                sb.append(startColor(Objects.requireNonNull(comp.color())));
            }

            // ## decoration
            // ### only start if prevComp didn't start
            if(comp.hasDecoration(TextDecoration.BOLD) && (prevComp == null || !prevComp.hasDecoration(TextDecoration.BOLD))) {
                sb.append(startTag(BOLD));
            }
            if(comp.hasDecoration(TextDecoration.ITALIC) && (prevComp == null || !prevComp.hasDecoration(TextDecoration.ITALIC))) {
                sb.append(startTag(ITALIC));
            }
            if(comp.hasDecoration(TextDecoration.OBFUSCATED) && (prevComp == null || !prevComp.hasDecoration(TextDecoration.OBFUSCATED))) {
                sb.append(startTag(OBFUSCATED));
            }
            if(comp.hasDecoration(TextDecoration.STRIKETHROUGH) && (prevComp == null || !prevComp.hasDecoration(TextDecoration.STRIKETHROUGH))) {
                sb.append(startTag(STRIKETHROUGH));
            }
            if(comp.hasDecoration(TextDecoration.UNDERLINED) && (prevComp == null || !prevComp.hasDecoration(TextDecoration.UNDERLINED))) {
                sb.append(startTag(UNDERLINED));
            }

            // ## hover
            // ### only start if prevComp didn't start the same one
            final HoverEvent<?> hov = comp.hoverEvent();
            if(hov != null && (prevComp == null || areDifferent(hov, prevComp.hoverEvent()))) {
                if(hov.value() instanceof Component) {
                    sb.append(startTag(String.format("%s" + SEPARATOR + "%s" + SEPARATOR + "\"%s\"", HOVER, HoverEvent.Action.NAMES.key(hov.action()), serialize((Component) hov.value()))));
                } else {
                    // TODO what to do here? warning? exception? toggleable strict mode?
                }
            }

            // ## click
            // ### only start if prevComp didn't start the same one
            final ClickEvent click = comp.clickEvent();
            if(click != null && (prevComp == null || areDifferent(click, prevComp.clickEvent()))) {
                sb.append(startTag(String.format("%s" + SEPARATOR + "%s" + SEPARATOR + "\"%s\"", CLICK, ClickEvent.Action.NAMES.key(click.action()), click.value())));
            }

            // ## insertion
            // ### only start if prevComp didn't start the same one
            final String insert = comp.insertion();
            if(insert != null && (prevComp == null || !insert.equals(prevComp.insertion()))) {
                sb.append(startTag(INSERTION + SEPARATOR + insert));
            }

            // ## font
            final Key font = comp.style().font();
            if(font != null && (prevComp == null || !font.equals(prevComp.style().font()))) {
                sb.append(startTag(FONT + SEPARATOR + font.asString()));
            }

            // # append text
            if(comp instanceof TextComponent) {
                sb.append(((TextComponent) comp).content());
            } else {
                handleDifferentComponent(comp, sb);
            }

            // # end tags

            // ## get next comp
            Component nextComp = null;
            if(i + 1 < components.size()) {
                nextComp = components.get(i + 1);
            }

            // ## color
            // ### only end color if next comp is white and current isn't
            if(nextComp != null && comp.color() != NamedTextColor.WHITE && comp.color() != null) {
                if(nextComp.color() == NamedTextColor.WHITE || nextComp.color() == null) {
                    sb.append(endColor(Objects.requireNonNull(comp.color())));
                }
            }

            // ## decoration
            // ### only end decoration if next tag is different
            if(nextComp != null) {
                if(comp.hasDecoration(TextDecoration.BOLD) && !nextComp.hasDecoration(TextDecoration.BOLD)) {
                    sb.append(endTag(BOLD));
                }
                if(comp.hasDecoration(TextDecoration.ITALIC) && !nextComp.hasDecoration(TextDecoration.ITALIC)) {
                    sb.append(endTag(ITALIC));
                }
                if(comp.hasDecoration(TextDecoration.OBFUSCATED) && !nextComp.hasDecoration(TextDecoration.OBFUSCATED)) {
                    sb.append(endTag(OBFUSCATED));
                }
                if(comp.hasDecoration(TextDecoration.STRIKETHROUGH) && !nextComp.hasDecoration(TextDecoration.STRIKETHROUGH)) {
                    sb.append(endTag(STRIKETHROUGH));
                }
                if(comp.hasDecoration(TextDecoration.UNDERLINED) && !nextComp.hasDecoration(TextDecoration.UNDERLINED)) {
                    sb.append(endTag(UNDERLINED));
                }
            }

            // ## hover
            // ### only end hover if next tag is different
            if(nextComp != null && comp.hoverEvent() != null) {
                if(areDifferent(Objects.requireNonNull(comp.hoverEvent()), nextComp.hoverEvent())) {
                    sb.append(endTag(HOVER));
                }
            }

            // ## click
            // ### only end click if next tag is different
            if(nextComp != null && comp.clickEvent() != null) {
                if(areDifferent(Objects.requireNonNull(comp.clickEvent()), nextComp.clickEvent())) {
                    sb.append(endTag(CLICK));
                }
            }

            // ## insertion
            // ### only end insertion if next tag is different
            if(nextComp != null && comp.insertion() != null) {
                if(!Objects.equals(comp.insertion(), nextComp.insertion())) {
                    sb.append(endTag(INSERTION));
                }
            }

            // ## font
            // ### only end insertion if next tag is different
            if(nextComp != null && comp.style().font() != null) {
                if(!Objects.equals(comp.style().font(), nextComp.style().font())) {
                    sb.append(endTag(FONT));
                }
            }
        }

        return sb.toString();
    }

    private static boolean areDifferent(final @NonNull ClickEvent c1, final @Nullable ClickEvent c2) {
        if(c2 == null) return true;
        return !c1.equals(c2) && (!c1.action().equals(c2.action()) || !c1.value().equals(c2.value()));
    }

    private static boolean areDifferent(final @NonNull HoverEvent<?> h1, final @Nullable HoverEvent<?> h2) {
        if(h2 == null) return true;
        return !h1.equals(h2) && (!h1.action().equals(h2.action())); // TODO also compare value
    }

    private static @NonNull String startColor(final @NonNull TextColor color) {
        if(color instanceof NamedTextColor) {
            return startTag(Objects.requireNonNull(NamedTextColor.NAMES.key((NamedTextColor) color)));
        } else {
            return startTag(COLOR + SEPARATOR + color.asHexString());
        }
    }

    private static @NonNull String endColor(final @NonNull TextColor color) {
        if(color instanceof NamedTextColor) {
            return endTag(Objects.requireNonNull(NamedTextColor.NAMES.key((NamedTextColor) color)));
        } else {
            return endTag(COLOR + SEPARATOR + color.asHexString());
        }
    }

    private static @NonNull String startTag(final @NonNull String content) {
        return TAG_START + content + TAG_END;
    }

    private static @NonNull String endTag(final @NonNull String content) {
        return TAG_START + CLOSE_TAG + content + TAG_END;
    }

    private static void handleDifferentComponent(final @NonNull Component component, final @NonNull StringBuilder sb) {
        if(component instanceof KeybindComponent) {
            sb.append(startTag(KEYBIND + SEPARATOR + ((KeybindComponent) component).keybind()));
        } else if(component instanceof TranslatableComponent) {
            sb.append(startTag(TRANSLATABLE + SEPARATOR + ((TranslatableComponent) component).key()));
        }
    }
}

