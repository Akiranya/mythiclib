package io.lumine.mythic.lib.comp.adventure.resolver.implementation;

import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.gradient.GradientBuilder;
import io.lumine.mythic.lib.comp.adventure.gradient.Interpolator;
import io.lumine.mythic.lib.comp.adventure.resolver.ContextTagResolver;
import io.lumine.mythic.lib.util.AdventureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class GradientResolver implements ContextTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argsQueue, @NotNull String context, @NotNull List<String> decorations) {
        if (!argsQueue.hasNext())
            return GradientBuilder.rgbGradient(context, Color.WHITE, Color.BLACK, 0, Interpolator.LINEAR, decorations);
        List<String> args = new ArrayList<>();
        while (argsQueue.hasNext())
            args.add(argsQueue.pop().value());
        double phase = getPhase(args);
        if (args.size() > 2)
            return GradientBuilder.multiRgbGradient(context, args.stream().map(AdventureUtils::color).toArray(Color[]::new), phase, Interpolator.LINEAR, decorations);
        final Color c1 = AdventureUtils.color(args.get(0));
        if (args.size() == 1)
            return GradientBuilder.rgbGradient(context, c1, Color.BLACK, phase, Interpolator.LINEAR, decorations);
        return GradientBuilder.rgbGradient(context, c1, AdventureUtils.color(args.get(1)), phase, Interpolator.LINEAR, decorations);
    }

    private double getPhase(List<String> args) {
        String lastArg = args.get(args.size() - 1);
        try {
            double phase = Double.parseDouble(lastArg);
            args.remove(args.size() - 1);
            return phase;
        } catch (NumberFormatException e) {
            return 1d;
        }
    }

}
