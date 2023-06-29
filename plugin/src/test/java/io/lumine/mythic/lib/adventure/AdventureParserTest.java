package io.lumine.mythic.lib.adventure;

import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import io.lumine.mythic.lib.comp.adventure.tag.implementation.*;
import io.lumine.mythic.lib.comp.adventure.tag.implementation.decorations.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class AdventureParserTest {

    @Test
    void testHexColorTag() {
        // Add tag parser
        final AdventureParser parser = new AdventureParser(true);
        final HexColorTag tag = new HexColorTag();
        parser.forceRegister(tag);

        // Invalid tag
        final String i1 = "This is a #FF0000red#FFFFFF text";
        Assertions.assertEquals(i1, parser.parse(i1));

        // Valid single tag
        final String i2 = "<#FFFFFF>This is a white text";
        final String i2Expected = "§x§F§F§F§F§F§FThis is a white text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Valid multiple tags
        final String i3 = "<#FFFFFF>This is a <#FF0000>red<#FFFFFF> text";
        final String i3Expected = "§x§F§F§F§F§F§FThis is a §x§F§F§0§0§0§0red§x§F§F§F§F§F§F text";
        Assertions.assertEquals(i3Expected, parser.parse(i3));

        // Invalid tags
        final String i4 = "<#FFFF>This is a <#FF000>red<#F1FFF> text";
        final String i4Expected = "<#FFFF>This is a <#FF000>red<#F1FFF> text";
        Assertions.assertEquals(i4Expected, parser.parse(i4));

        // Valid HEX tag
        final String i5 = "<HEXFF0000>This is a red text";
        final String i5Expected = "§x§F§F§0§0§0§0This is a red text";
        Assertions.assertEquals(i5Expected, parser.parse(i5));

        // Invalid HEX tag
        final String i6 = "<HEXFF000>This is a red text";
        final String i6Expected = "<HEXFF000>This is a red text";
        Assertions.assertEquals(i6Expected, parser.parse(i6));

        // Remove tag parser
        parser.remove(tag);
    }

    @Test
    void testVanillaColors() {
        // Add tag parser
        final AdventureParser parser = new AdventureParser(true);
        final VanillaColorTag tag = new VanillaColorTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<RED>This is a red text";
        final String i1Expected = "§cThis is a red text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Invalid tag
        final String i2 = "This is a red text";
        final String i2Expected = "This is a red text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Multiple valid tags
        final String i3 = "<RED>This is a <GREEN>green<RED> text";
        final String i3Expected = "§cThis is a §agreen§c text";
        Assertions.assertEquals(i3Expected, parser.parse(i3));

        // Multiple invalid tags
        final String i4 = "This is a <GREN>green<RE> text";
        final String i4Expected = "This is a <GREN>green<RE> text";
        Assertions.assertEquals(i4Expected, parser.parse(i4));

        // Decorations
        final String i5 = "<RED>This is a <GREEN>green<RED> <BOLD>bold<RED> text";
        final String i5Expected = "§cThis is a §agreen§c <BOLD>bold§c text";
        Assertions.assertEquals(i5Expected, parser.parse(i5));

        // Remove tag parser
        parser.remove(tag);
    }

    @Test
    void testObfuscatedTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        ObfuscatedTag tag = new ObfuscatedTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<obfuscated>This is a obfuscated text";
        final String i1Expected = "§kThis is a obfuscated text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Alias tag
        final String i2 = "<OBF>This is a obfuscated text";
        final String i2Expected = "§kThis is a obfuscated text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    void testBoldTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        BoldTag tag = new BoldTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<bold>This is a bold text";
        final String i1Expected = "§lThis is a bold text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Alias tag
        final String i2 = "<B>This is a bold text";
        final String i2Expected = "§lThis is a bold text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    void testItalicTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        ItalicTag tag = new ItalicTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<italic>This is a italic text";
        final String i1Expected = "§oThis is a italic text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Alias tag
        final String i2 = "<I>This is a italic text";
        final String i2Expected = "§oThis is a italic text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testResetTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        ResetTag tag = new ResetTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<reset>This is a reset text";
        final String i1Expected = "§rThis is a reset text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Alias tag
        final String i2 = "<R>This is a reset text";
        final String i2Expected = "§rThis is a reset text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testUnderlineTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        UnderlineTag tag = new UnderlineTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<underlined>This is a underline text";
        final String i1Expected = "§nThis is a underline text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Alias tag
        final String i2 = "<U>This is a underline text";
        final String i2Expected = "§nThis is a underline text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testStrikethroughTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        StrikethroughTag tag = new StrikethroughTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<strikethrough>This is a strikethrough text";
        final String i1Expected = "§mThis is a strikethrough text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Alias tag
        final String i2 = "<ST>This is a strikethrough text";
        final String i2Expected = "§mThis is a strikethrough text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testAdventureColors() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        AdventureColorTag tag = new AdventureColorTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<color:red>This is a red text";
        final String i1Expected = "§cThis is a red text";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Invalid tag
        final String i2 = "<color:invalid>This is a red text";
        final String i2Expected = "This is a red text";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Hex tag
        final String i3 = "<color:#FF0000>This is a red text";
        final String i3Expected = "§x§F§F§0§0§0§0This is a red text";
        Assertions.assertEquals(i3Expected, parser.parse(i3));

        // Hex tag without #
        final String i4 = "<color:FF0000>This is a red text";
        final String i4Expected = "§x§F§F§0§0§0§0This is a red text";
        Assertions.assertEquals(i4Expected, parser.parse(i4));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testClosedTag() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        AdventureColorTag tag = new AdventureColorTag();
        parser.forceRegister(tag);

        // Valid tag
        final String i1 = "<color:red>This is a red text</color>Hey";
        final String i1Expected = "§cThis is a red text§rHey";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // Multiple tags
        final String i2 = "<color:red>This is a red text</color><color:blue>This is a blue text</color>";
        final String i2Expected = "§cThis is a red text§r§9This is a blue text§r";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testNewline() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        NewlineTag tag = new NewlineTag();
        parser.forceRegister(tag);

        // Valid tag

        Collection<String> i1 = Arrays.asList("Hey!", "What a good<newline><newline>day!", "Init?", "Yeah!<newline>It a pretty day<newline>We could go out, nah?");
        Collection<String> result = parser.parse(i1);
        Assertions.assertEquals(result.size(), 8);

        // Alias tag
        Collection<String> i2 = Arrays.asList("Hey!", "What a good<br><br>day!", "Init?", "Yeah!<br>It a pretty day<br>We could go out, nah?");
        Collection<String> result2 = parser.parse(i2);
        Assertions.assertEquals(result2.size(), 8);

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testTagContext() {
        AdventureParser specialParser = new AdventureParser(true);
        specialParser.forceRegister(new GradientTag());
        specialParser.forceRegister(new VanillaColorTag());
        specialParser.forceRegister(new HexColorTag());
        specialParser.forceRegister(new AdventureColorTag());
        specialParser.forceRegister(new NewlineTag());
        specialParser.forceRegister(new BoldTag());
        specialParser.forceRegister(new ItalicTag());
        specialParser.forceRegister(new ObfuscatedTag());
        specialParser.forceRegister(new ResetTag());
        specialParser.forceRegister(new StrikethroughTag());
        specialParser.forceRegister(new UnderlineTag());


        // Single closed context tag
        final String i1 = "<gradient>Test</gradient>";
        final String i1Expected = "§x§f§f§f§f§f§fT§x§a§a§a§a§a§ae§x§5§5§5§5§5§5s§x§0§0§0§0§0§0t§r";
        Assertions.assertEquals(i1Expected, specialParser.parse(i1));

        // Single context tag unclosed
        final String i2 = "<gradient>Test";
        final String i2Expected = "§x§f§f§f§f§f§fT§x§a§a§a§a§a§ae§x§5§5§5§5§5§5s§x§0§0§0§0§0§0t";
        Assertions.assertEquals(i2Expected, specialParser.parse(i2));

        // Single context tag & normal tag
        final String i3 = "<gradient>Test <color:red>Hey";
        final String i3Expected = "§x§f§f§f§f§f§fT§x§b§f§b§f§b§fe§x§8§0§8§0§8§0s§x§4§0§4§0§4§0t§x§0§0§0§0§0§0 §cHey";
        Assertions.assertEquals(i3Expected, specialParser.parse(i3));

        // Multiple context tag
        final String i4 = "<gradient>Test <gradient:red:blue>Hey";
        final String i4Expected = "§x§f§f§f§f§f§fT§x§b§f§b§f§b§fe§x§8§0§8§0§8§0s§x§4§0§4§0§4§0t§x§0§0§0§0§0§0 §x§f§f§5§5§5§5H§x§a§a§5§5§a§ae§x§5§5§5§5§f§fy";
        Assertions.assertEquals(i4Expected, specialParser.parse(i4));
    }

    @Test
    public void testGradient() {
        // Add tag
        final AdventureParser parser = new AdventureParser(true);
        GradientTag tag = new GradientTag();
        parser.forceRegister(tag);

        // Default gradient
        final String i1 = "<gradient>This is a gradient text</gradient>";
        final String i1Expected = "§x§f§f§f§f§f§fT§x§f§3§f§3§f§3h§x§e§8§e§8§e§8i§x§d§c§d§c§d§cs§x§d§1§d§1§d§1 §x§c§5§c§5§c§5i§x§b§9§b§9§b§9s§x§a§e§a§e§a§e §x§a§2§a§2§a§2a§x§9§7§9§7§9§7 §x§8§b§8§b§8§bg§x§7§f§7§f§7§fr§x§7§4§7§4§7§4a§x§6§8§6§8§6§8d§x§5§d§5§d§5§di§x§5§1§5§1§5§1e§x§4§6§4§6§4§6n§x§3§a§3§a§3§at§x§2§e§2§e§2§e §x§2§3§2§3§2§3t§x§1§7§1§7§1§7e§x§0§c§0§c§0§cx§x§0§0§0§0§0§0t§r";
        Assertions.assertEquals(i1Expected, parser.parse(i1));

        // 2 color gradient
        final String i2 = "<gradient:red:blue>This is a gradient text</gradient>";
        final String i2Expected = "§x§f§f§5§5§5§5T§x§f§7§5§5§5§dh§x§f§0§5§5§6§4i§x§e§8§5§5§6§cs§x§e§0§5§5§7§4 §x§d§8§5§5§7§ci§x§d§1§5§5§8§3s§x§c§9§5§5§8§b §x§c§1§5§5§9§3a§x§b§9§5§5§9§b §x§b§2§5§5§a§2g§x§a§a§5§5§a§ar§x§a§2§5§5§b§2a§x§9§b§5§5§b§9d§x§9§3§5§5§c§1i§x§8§b§5§5§c§9e§x§8§3§5§5§d§1n§x§7§c§5§5§d§8t§x§7§4§5§5§e§0 §x§6§c§5§5§e§8t§x§6§4§5§5§f§0e§x§5§d§5§5§f§7x§x§5§5§5§5§f§ft§r";
        Assertions.assertEquals(i2Expected, parser.parse(i2));

        // 3 color gradient
        final String i3 = "<gradient:red:blue:green>This is a gradient text</gradient>";
        final String i3Expected = "§x§f§f§5§5§5§5T§x§e§e§5§5§6§6h§x§d§d§5§5§7§7i§x§c§c§5§5§8§8s§x§b§b§5§5§9§9 §x§a§a§5§5§a§ai§x§9§9§5§5§b§bs§x§8§8§5§5§c§c §x§7§7§5§5§d§da§x§6§6§5§5§e§e §x§5§5§5§5§f§fg§x§5§5§5§5§f§fr§x§5§5§6§6§e§ea§x§5§5§7§7§d§dd§x§5§5§8§8§c§ci§x§5§5§9§9§b§be§x§5§5§a§a§a§an§x§5§5§b§b§9§9t§x§5§5§c§c§8§8 §x§5§5§d§d§7§7t§x§5§5§e§e§6§6e§x§5§5§f§f§5§5x§x§5§5§f§f§5§5t§r";
        Assertions.assertEquals(i3Expected, parser.parse(i3));

        // 4 color gradient
        final String i4 = "<gradient:red:blue:green:yellow>This is a gradient text</gradient>";
        final String i4Expected = "§x§f§f§5§5§5§5T§x§e§3§5§5§7§1h§x§c§6§5§5§8§ei§x§a§a§5§5§a§as§x§8§e§5§5§c§6 §x§7§1§5§5§e§3i§x§5§5§5§5§f§fs§x§5§5§5§5§f§f §x§5§5§7§1§e§3a§x§5§5§8§e§c§6 §x§5§5§a§a§a§ag§x§5§5§c§6§8§er§x§5§5§e§3§7§1a§x§5§5§f§f§5§5d§x§5§5§f§f§5§5i§x§7§1§f§f§5§5e§x§8§e§f§f§5§5n§x§a§a§f§f§5§5t§x§c§6§f§f§5§5 §x§e§3§f§f§5§5t§x§f§f§f§f§5§5e§x§f§f§f§f§5§5xt§r";
        Assertions.assertEquals(i4Expected, parser.parse(i4));

        // Remove tag
        parser.remove(tag);
    }

    @Test
    public void testStripColor() {
        final AdventureParser parser = new AdventureParser(true);

        final String i1 = "§cThis is a red text";
        final String i1Expected = "This is a red text";
        Assertions.assertEquals(i1Expected, parser.stripColors(i1));


        final String i2 = "§cThis is a red text§r";
        final String i2Expected = "This is a red text";
        Assertions.assertEquals(i2Expected, parser.stripColors(i2));

        final String i3 = "<red>This is a red text<reset><red>";
        final String i3Expected = "This is a red text";
        Assertions.assertEquals(i3Expected, parser.stripColors(i3));

        final String i4 = "<gradient:red:blue>This is a gradient text";
        final String i4Expected = "This is a gradient text";
        Assertions.assertEquals(i4Expected, parser.stripColors(i4));
    }

    @Test
    public void testLastColor() {
        final AdventureParser parser = new AdventureParser(true);
        parser.forceRegister(new GradientTag());
        parser.forceRegister(new VanillaColorTag());
        parser.forceRegister(new HexColorTag());
        parser.forceRegister(new AdventureColorTag());
        parser.forceRegister(new BoldTag());
        parser.forceRegister(new ResetTag());

        final String i1 = "§cThis is a red text";
        final String i1Expected = "§c";
        Assertions.assertEquals(i1Expected, parser.lastColor(i1, true));

        final String i2 = "§cThis is a red text§r";
        final String i2Expected = "§r";
        Assertions.assertEquals(i2Expected, parser.lastColor(i2, true));

        final String i3 = "<red>This is a red text<reset></red>";
        final String i3Expected = "<reset>";
        Assertions.assertEquals(i3Expected, parser.lastColor(i3, true));

        final String i4 = "<gradient:red:blue>This is a gradient text";
        final String i4Expected = "<gradient:red:blue>";
        Assertions.assertEquals(i4Expected, parser.lastColor(i4, true));

        final String i5 = "<#FFFFFF>This is a gradient text";
        final String i5Expected = "<#FFFFFF>";
        Assertions.assertEquals(i5Expected, parser.lastColor(i5, true));

        final String i6 = "<color:blue>This is a gradient text";
        final String i6Expected = "<color:blue>";
        Assertions.assertEquals(i6Expected, parser.lastColor(i6, true));

        final String i7 = "<bold>This is a gradient text";
        final String i7Expected = "<bold>";
        Assertions.assertEquals(i7Expected, parser.lastColor(i7, true));

        final String i8 = "<bold>This is a gradient text";
        final String i8Expected = "";
        Assertions.assertEquals(i8Expected, parser.lastColor(i8, false));

        final String i9 = "<red>This is a <gradient>gradient <blue><bold>bold";
        final String i9Expected = "<blue><bold>";
        final String i9Expected1 = "<blue>";
        Assertions.assertEquals(i9Expected, parser.lastColor(i9, true));
        Assertions.assertEquals(i9Expected1, parser.lastColor(i9, false));

        final String i10 = "<red>This is a <gradient>gradient <blue>text§abold";
        final String i10Expected = "§a";
        Assertions.assertEquals(i10Expected, parser.lastColor(i10, false));

        final String i11 = "This is a plain text";
        final String i11Expected = "";
        Assertions.assertEquals(i11Expected, parser.lastColor(i11, true));

        final String i12 = "<red>This is a <gradient>gradient <bold><blue>bold";
        final String i12Expected = "<bold><blue>";
        final String i12Expected1 = "<blue>";
        Assertions.assertEquals(i12Expected, parser.lastColor(i12, true));
        Assertions.assertEquals(i12Expected1, parser.lastColor(i12, false));

        final String i13 = "<red>This is a <gradient>gradient <underlined><yellow><bold><blue><bold>bold";
        final String i13Expected = "<bold><blue><bold>";
        final String i13Expected1 = "<blue>";
        Assertions.assertEquals(i13Expected, parser.lastColor(i13, true));
        Assertions.assertEquals(i13Expected1, parser.lastColor(i13, false));

        final String i14 = "<HEXFFFFFF>This is a gradient text";
        final String i14Expected = "<HEXFFFFFF>";
        Assertions.assertEquals(i14Expected, parser.lastColor(i14, true));

        final String i15 = "<strikethrough><italic><HEXFFFFFF><bold><obfuscated>This is a gradient text";
        final String i15Expected = "<strikethrough><italic><HEXFFFFFF><bold><obfuscated>";
        Assertions.assertEquals(i15Expected, parser.lastColor(i15, true));

        final String i16 = "<strikethrough><italic><HEXFFFFFF><obfuscated>This is a gradient text";
        final String i16Expected = "<HEXFFFFFF>";
        Assertions.assertEquals(i16Expected, parser.lastColor(i16, false));

        // Legacy colors
        final String i17 = "&c&lThis is a red text";
        final String i17Expected = "§c§l";
        Assertions.assertEquals(i17Expected, parser.lastColor(i17, true));
    }

    @Test
    void testGradientWithDecorations() {
        AdventureParser parser = new AdventureParser(true);
        parser.forceRegister(new GradientTag());
        parser.forceRegister(new BoldTag());
        parser.forceRegister(new ResetTag());

        // Basic gradient with a bold tag in the context
        String input = "<gradient><bold>This is a gradient text";
        String expected = "§x§f§f§f§f§f§f§lT§x§f§3§f§3§f§3§lh§x§e§8§e§8§e§8§li§x§d§c§d§c§d§c§ls§x§d§1§d§1§d§1§l §x§c§5§c§5§c§5§li§x§b§9§b§9§b§9§ls§x§a§e§a§e§a§e§l §x§a§2§a§2§a§2§la§x§9§7§9§7§9§7§l §x§8§b§8§b§8§b§lg§x§7§f§7§f§7§f§lr§x§7§4§7§4§7§4§la§x§6§8§6§8§6§8§ld§x§5§d§5§d§5§d§li§x§5§1§5§1§5§1§le§x§4§6§4§6§4§6§ln§x§3§a§3§a§3§a§lt§x§2§e§2§e§2§e§l §x§2§3§2§3§2§3§lt§x§1§7§1§7§1§7§le§x§0§c§0§c§0§c§lx§x§0§0§0§0§0§0§lt";
        Assertions.assertEquals(expected, parser.parse(input));

//        // Basic gradient with a bold tag in the middle of the gradient
//        input = "<gradient>This is a <bold>gradient text";
//        expected = "§x§f§f§f§f§f§f§§x§f§8§f§8§f§8l§x§f§2§f§2§f§2g§x§e§b§e§b§e§b§§x§e§4§e§4§e§4l§x§d§d§d§d§d§dr§x§d§7§d§7§d§7§§x§d§0§d§0§d§0l§x§c§9§c§9§c§9a§x§c§3§c§3§c§3§§x§b§c§b§c§b§cl§x§b§5§b§5§b§5d§x§a§e§a§e§a§e§§x§a§8§a§8§a§8l§x§a§1§a§1§a§1i§x§9§a§9§a§9§a§§x§9§4§9§4§9§4l§x§8§d§8§d§8§de§x§8§6§8§6§8§6§§x§8§0§8§0§8§0l§x§7§9§7§9§7§9n§x§7§2§7§2§7§2§§x§6§b§6§b§6§bl§x§6§5§6§5§6§5t§x§5§e§5§e§5§e§§x§5§7§5§7§5§7l§x§5§1§5§1§5§1 §x§4§a§4§a§4§a§§x§4§3§4§3§4§3l§x§3§c§3§c§3§ct§x§3§6§3§6§3§6§§x§2§f§2§f§2§fl§x§2§8§2§8§2§8e§x§2§2§2§2§2§2§§x§1§b§1§b§1§bl§x§1§4§1§4§1§4x§x§0§d§0§d§0§d§§x§0§7§0§7§0§7l§x§0§0§0§0§0§0t";
//        Assertions.assertEquals(expected, parser.parse(input));
    }

    @Test
    void testSpecial() {
        AdventureParser parser = new AdventureParser(true);
        parser.forceRegister(new GradientTag());
        parser.forceRegister(new AdventureColorTag());
        parser.forceRegister(new HexColorTag());

        String input = "<#a1c7a1>❍ <gradient:#aff0af:#d5f2aa>Amplistone <#e0e0e0>Moon <#a1c7a1>❍";
        Assertions.assertFalse(parser.parse(input).contains(">"));
    }
}