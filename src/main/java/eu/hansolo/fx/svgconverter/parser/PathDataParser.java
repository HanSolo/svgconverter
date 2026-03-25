package eu.hansolo.fx.svgconverter.parser;

import eu.hansolo.fx.svgconverter.model.SVGPath.*;
import eu.hansolo.fx.svgconverter.model.SVGPath.ArcTo;
import eu.hansolo.fx.svgconverter.model.SVGPath.ClosePath;
import eu.hansolo.fx.svgconverter.model.SVGPath.CubicCurveTo;
import eu.hansolo.fx.svgconverter.model.SVGPath.LineTo;
import eu.hansolo.fx.svgconverter.model.SVGPath.MoveTo;
import eu.hansolo.fx.svgconverter.model.SVGPath.PathCommand;
import eu.hansolo.fx.svgconverter.model.SVGPath.QuadraticCurveTo;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SVG path data strings into PathCommand objects.
 * Handles all SVG path commands: M, L, H, V, C, S, Q, T, A, Z (and lowercase variants).
 */
public class PathDataParser {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("[MmLlHhVvCcSsQqTtAaZz]");
    private static final Pattern NUMBER_PATTERN  = Pattern.compile("-?\\d*\\.?\\d+(?:[eE][+-]?\\d+)?");
    
    private double currentX     = 0;
    private double currentY     = 0;
    private double startX       = 0;
    private double startY       = 0;
    private double lastControlX = 0;
    private double lastControlY = 0;
    private char   lastCommand  = ' ';
    
    /**
     * Parse SVG path data string into a list of PathCommand objects.
     */
    public List<PathCommand> parse(final String pathData) {
        if (pathData == null || pathData.trim().isEmpty()) { return new ArrayList<>(); }

        final List<PathCommand> commands = new ArrayList<>();
        
        // Reset state
        currentX     = 0;
        currentY     = 0;
        startX       = 0;
        startY       = 0;
        lastControlX = 0;
        lastControlY = 0;
        lastCommand  = ' ';
        
        // Tokenize the path data
        final String  normalized     = pathData.trim().replaceAll(",", " ");
        final Matcher commandMatcher = COMMAND_PATTERN.matcher(normalized);
        
        int lastEnd         = 0;
        char currentCommand = ' ';
        
        while (commandMatcher.find()) {
            // Process previous command's coordinates
            if (currentCommand != ' ') {
                final String coords = normalized.substring(lastEnd, commandMatcher.start()).trim();
                if (!coords.isEmpty()) { commands.addAll(parseCommand(currentCommand, coords)); }
            }
            currentCommand = commandMatcher.group().charAt(0);
            lastEnd        = commandMatcher.end();
        }
        
        // Process last command
        if (currentCommand != ' ') {
            final String coords = normalized.substring(lastEnd).trim();
            if (!coords.isEmpty() || currentCommand == 'Z' || currentCommand == 'z') {
                commands.addAll(parseCommand(currentCommand, coords));
            }
        }
        return commands;
    }
    
    private List<PathCommand> parseCommand(final char command, final String coords) {
        final List<PathCommand> commands     = new ArrayList<>();
        final boolean           relative     = Character.isLowerCase(command);
        final char              upperCommand = Character.toUpperCase(command);
        final List<Double>      numbers      = parseNumbers(coords);
        switch (upperCommand) {
            case 'M' -> commands.addAll(parseMoveTo(numbers, relative));
            case 'L' -> commands.addAll(parseLineTo(numbers, relative));
            case 'H' -> commands.addAll(parseHLineTo(numbers, relative));
            case 'V' -> commands.addAll(parseVLineTo(numbers, relative));
            case 'C' -> commands.addAll(parseCubicCurveTo(numbers, relative));
            case 'S' -> commands.addAll(parseSmoothCubicCurveTo(numbers, relative));
            case 'Q' -> commands.addAll(parseQuadraticCurveTo(numbers, relative));
            case 'T' -> commands.addAll(parseSmoothQuadraticCurveTo(numbers, relative));
            case 'A' -> commands.addAll(parseArcTo(numbers, relative));
            case 'Z' -> commands.add(new ClosePath());
        }
        lastCommand = command;
        return commands;
    }
    
    private List<Double> parseNumbers(final String coords) {
        final List<Double> numbers = new ArrayList<>();
        final Matcher      matcher = NUMBER_PATTERN.matcher(coords);
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        return numbers;
    }
    
    private List<PathCommand> parseMoveTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 2) {
            double x = numbers.get(i);
            double y = numbers.get(i + 1);
            
            if (relative && i > 0) {
                // After first moveto, subsequent pairs are treated as lineto
                x += currentX;
                y += currentY;
                commands.add(new LineTo(x, y, false));
            } else {
                if (relative) {
                    x += currentX;
                    y += currentY;
                }
                commands.add(new MoveTo(x, y, false));
                startX = x;
                startY = y;
            }
            
            currentX     = x;
            currentY     = y;
            lastControlX = x;
            lastControlY = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseLineTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 2) {
            double x = numbers.get(i);
            double y = numbers.get(i + 1);
            
            if (relative) {
                x += currentX;
                y += currentY;
            }
            
            commands.add(new LineTo(x, y, false));
            currentX     = x;
            currentY     = y;
            lastControlX = x;
            lastControlY = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseHLineTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (double x : numbers) {
            if (relative) {
                x += currentX;
            }
            
            commands.add(new LineTo(x, currentY, false));
            currentX     = x;
            lastControlX = x;
            lastControlY = currentY;
        }
        return commands;
    }
    
    private List<PathCommand> parseVLineTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (double y : numbers) {
            if (relative) {
                y += currentY;
            }
            
            commands.add(new LineTo(currentX, y, false));
            currentY     = y;
            lastControlX = currentX;
            lastControlY = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseCubicCurveTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 6) {
            double x1 = numbers.get(i);
            double y1 = numbers.get(i + 1);
            double x2 = numbers.get(i + 2);
            double y2 = numbers.get(i + 3);
            double x  = numbers.get(i + 4);
            double y  = numbers.get(i + 5);
            
            if (relative) {
                x1 += currentX;
                y1 += currentY;
                x2 += currentX;
                y2 += currentY;
                x  += currentX;
                y  += currentY;
            }
            
            commands.add(new CubicCurveTo(x1, y1, x2, y2, x, y, false));
            lastControlX = x2;
            lastControlY = y2;
            currentX     = x;
            currentY     = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseSmoothCubicCurveTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 4) {
            // Infer first control point from last control point
            double x1 = 2 * currentX - lastControlX;
            double y1 = 2 * currentY - lastControlY;
            
            double x2 = numbers.get(i);
            double y2 = numbers.get(i + 1);
            double x  = numbers.get(i + 2);
            double y  = numbers.get(i + 3);
            
            if (relative) {
                x2 += currentX;
                y2 += currentY;
                x  += currentX;
                y  += currentY;
            }
            
            commands.add(new CubicCurveTo(x1, y1, x2, y2, x, y, false));
            lastControlX = x2;
            lastControlY = y2;
            currentX     = x;
            currentY     = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseQuadraticCurveTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 4) {
            double x1 = numbers.get(i);
            double y1 = numbers.get(i + 1);
            double x  = numbers.get(i + 2);
            double y  = numbers.get(i + 3);
            
            if (relative) {
                x1 += currentX;
                y1 += currentY;
                x  += currentX;
                y  += currentY;
            }
            
            commands.add(new QuadraticCurveTo(x1, y1, x, y, false));
            lastControlX = x1;
            lastControlY = y1;
            currentX     = x;
            currentY     = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseSmoothQuadraticCurveTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 2) {
            // Infer control point from last control point
            double x1 = 2 * currentX - lastControlX;
            double y1 = 2 * currentY - lastControlY;
            
            double x = numbers.get(i);
            double y = numbers.get(i + 1);
            
            if (relative) {
                x += currentX;
                y += currentY;
            }
            
            commands.add(new QuadraticCurveTo(x1, y1, x, y, false));
            lastControlX = x1;
            lastControlY = y1;
            currentX     = x;
            currentY     = y;
        }
        return commands;
    }
    
    private List<PathCommand> parseArcTo(final List<Double> numbers, final boolean relative) {
        final List<PathCommand> commands = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i += 7) {
            double  rx       = numbers.get(i);
            double  ry       = numbers.get(i + 1);
            double  rotation = numbers.get(i + 2);
            boolean largeArc = numbers.get(i + 3) != 0;
            boolean sweep    = numbers.get(i + 4) != 0;
            double  x        = numbers.get(i + 5);
            double  y        = numbers.get(i + 6);
            
            if (relative) {
                x += currentX;
                y += currentY;
            }
            
            commands.add(new ArcTo(rx, ry, rotation, largeArc, sweep, x, y, false));
            currentX     = x;
            currentY     = y;
            lastControlX = x;
            lastControlY = y;
        }
        return commands;
    }
}
