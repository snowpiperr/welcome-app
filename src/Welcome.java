import static java.util.stream.Collectors.toList;

import java.awt.Color;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;

import edu.macalester.graphics.CanvasWindow;
import edu.macalester.graphics.FontStyle;
import edu.macalester.graphics.GraphicsObject;
import edu.macalester.graphics.GraphicsText;

/**
 * A lively animation to let you know your development environment is working!
 */
public class Welcome {
    private static final double
        PHASE_SPREAD = 0.5,
        INITIAL_TIME = -10,
        REGULAR_SPEED = 0.025,
        SLOW_MO_SPEED = 0.005,
        SLOW_MO_TIGHTNESS = 3,
        SLOW_MO_CURVE = 5;

    static final Random rand = new Random();

    private final CanvasWindow canvas;
    private final List<FlyingLetter> letters;
    private final double margin = 32;

    public static void main(String[] args) {
        new Welcome("macalester");
    }

    public Welcome(String message) {
        canvas = new CanvasWindow("Welcome to COMP 127", 600, 400);
        canvas.setBackground(Color.BLACK);

        letters = message.chars().mapToObj(FlyingLetter::new).collect(toList());

        double phasing = rand.nextDouble() * PHASE_SPREAD;
        int index = 0;
        for (FlyingLetter letter : letters) {
            // Horizontal curve = same frequency for all letters, but with different phases so that
            // letters are evenly spaced across at t = 0
            letter.getLissajousX().setCycle(1, Math.asin((index + 1.0) / (letters.size() + 1.0) * 2 - 1));

            // Vertical curve = higher frequency for letters later in the word
            letter.getLissajousY().setCycle((index * phasing + 1), 0);

            // Letters fly across the whole window minus margin
            letter.getLissajousX().setRange(margin, canvas.getWidth() - margin);
            letter.getLissajousY().setRange(margin, canvas.getHeight() - margin);
            letter.getLissajousX().setTime(INITIAL_TIME);
            letter.getLissajousY().setTime(INITIAL_TIME);

            canvas.add(letter.getGraphic());

            index++;
        }

        canvas.animate(this::doAnimationStep);
    }

    private void doAnimationStep() {
        // Slow down when letters are vertically close together
        DoubleSummaryStatistics stats = letters.stream()
            .mapToDouble((l) -> l.getGraphic().getCenter().getY())
            .summaryStatistics();
        double verticalSpread = (stats.getMax() - stats.getMin()) / canvas.getHeight();
        double dt = (1 - 1 / (Math.pow(SLOW_MO_TIGHTNESS * verticalSpread, SLOW_MO_CURVE) + 1))
            * REGULAR_SPEED + SLOW_MO_SPEED;

        for (FlyingLetter letter : letters) {
            letter.update(dt);
        }
    }
}

/**
 * An animated letter that follows a lissajous curve.
 */
class FlyingLetter {
    private static final double
        COLOR_SPEED = 0.000006,
        SPARKLE_CURVE = 1.6,
        FONT_SIZE = 32;

    private final GraphicsText graphic;
    private final SineWave lissajousX, lissajousY;
    private double hue;

    /**
     * Creates a letter with a random color.
     * @param letter A Unicode codepoint for a letter
     */
    FlyingLetter(int letter) {
        graphic = new GraphicsText(Character.toString(letter), 0, 0);
        graphic.setFont("Verdana", FontStyle.BOLD, FONT_SIZE);
        hue = Welcome.rand.nextFloat();

        lissajousX = new SineWave();
        lissajousY = new SineWave();
    }

    GraphicsObject getGraphic() {
        return graphic;
    }

    SineWave getLissajousX() {
        return lissajousX;
    }

    SineWave getLissajousY() {
        return lissajousY;
    }

    /**
     * Moves the letter to the appropriate position for time t.
     */
    void update(double dt) {
        graphic.setCenter(
            lissajousX.update(dt),
            lissajousY.update(dt));
        hue = (hue + 1 / Math.pow(dt, SPARKLE_CURVE) * COLOR_SPEED) % 1;
        graphic.setFillColor(Color.getHSBColor((float) hue, 1, 1));
    }
}

/**
 * A scaled sine wave with customizable frequency, phase, and range.
 */
class SineWave {
    private double wavelength, offset;
    private double min, max;
    private double t;  // time

    void setCycle(double wavelength, double offset) {
        this.wavelength = wavelength;
        this.offset = offset;
    }
    
    void setRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    void setTime(double t) {
        this.t = t;
    }

    /**
     * Returns the value of the wave after adding dt to time.
     */
    double update(double dt) {
        t = (t + dt) % (Math.PI * 2 / wavelength);
        return (Math.sin(t * wavelength + offset) + 1) / 2 * (max - min) + min;
    }
}
