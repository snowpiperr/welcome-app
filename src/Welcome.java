import static java.util.stream.Collectors.toList;

import java.awt.Color;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;

import comp127graphics.CanvasWindow;
import comp127graphics.FontStyle;
import comp127graphics.GraphicsObject;
import comp127graphics.GraphicsText;

/**
 * A lively animation to let you know your development environment is working!
 */
public class Welcome {
    static final Random rand = new Random();

    private final CanvasWindow canvas;
    private final List<FlyingLetter> letters;
    private final double margin = 32;
    private double t = -10;

    public static void main(String[] args) {
        new Welcome("macalester");
    }

    public Welcome(String message) {
        canvas = new CanvasWindow("Welcome to COMP 127", 600, 400);
        canvas.setBackground(Color.BLACK);

        letters = message.chars().mapToObj(FlyingLetter::new).collect(toList());

        double phasing = rand.nextDouble() / 2;
        int index = 0;
        for (FlyingLetter letter : letters) {
            letter.getLissajousX().setCycle(1, Math.asin((index + 1.0) / (letters.size() + 1.0) * 2 - 1));
            letter.getLissajousY().setCycle((index * phasing + 1), 0);
            letter.getLissajousX().setRange(margin, canvas.getWidth() - margin);
            letter.getLissajousY().setRange(margin, canvas.getHeight() - margin);

            canvas.add(letter.getGraphic());

            index++;
        }

        canvas.animate(this::doAnimationStep);
    }

    private void doAnimationStep() {
        for (FlyingLetter letter : letters) {
            letter.update(t);
        }

        DoubleSummaryStatistics stats = letters.stream()
            .mapToDouble((l) -> l.getGraphic().getCenter().getY())
            .summaryStatistics();
        t += Math.pow((stats.getMax() - stats.getMin()) / canvas.getHeight(), 0.4) * 0.02 + 0.005;
    }
}

/**
 * An animated letter that follows a lissajous curve.
 */
class FlyingLetter {

    private final GraphicsText graphic;
    private final SineWave lissajousX, lissajousY;
    private final double hueOffset;

    /**
     * Creates a letter with a random color.
     * @param letter A Unicode codepoint for a letter
     */
    FlyingLetter(int letter) {
        graphic = new GraphicsText(Character.toString(letter), 0, 0);
        graphic.setFont("Verdana", FontStyle.BOLD, 32);
        hueOffset = Welcome.rand.nextFloat();

        lissajousX = new SineWave();
        lissajousY = new SineWave();
    }

    public SineWave getLissajousX() {
        return lissajousX;
    }

    public SineWave getLissajousY() {
        return lissajousY;
    }

    void update(double t) {
        graphic.setCenter(
            lissajousX.getValueAt(t),
            lissajousY.getValueAt(t));
        graphic.setFillColor(Color.getHSBColor((float) (hueOffset + t / 20) % 1, 1, 1));
    }

    public GraphicsObject getGraphic() {
        return graphic;
    }
}

/**
 * A scaled sine wave with customizable frequency, phase, and range.
 */
class SineWave {
    private double wavelength, offset;
    private double min, max;

    void setCycle(double wavelength, double offset) {
        this.wavelength = wavelength;
        this.offset = offset;
    }
    
    void setRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    double getValueAt(double t) {
        return (Math.sin(t * wavelength + offset) + 1) / 2 * (max - min) + min;
    }
}
