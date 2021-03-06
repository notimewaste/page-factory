package ru.sbtqa.tag.pagefactory.support;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.qautils.properties.Props;

public class ScreenShooter {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenShooter.class);

    /**
     * Takes screenshot with driver
     *
     * @return screenshot in byte array
     */
    public static byte[] takeWithDriver() {
        return ((TakesScreenshot) PageFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Takes whole screen screenshot
     *
     * @return screenshot in byte array
     */
    public static byte[] takeRaw() {
        try {
            Rectangle screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage picture = new Robot().createScreenCapture(screenBounds);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(picture, "png", bytes);
            return bytes.toByteArray();
        } catch (AWTException | IOException ex) {
            LOG.error("Failed to get full screenshot on test failure because of IOException", ex);
            return "".getBytes();
        }
    }

    /**
     * Takes screenshot as indicated in application.properties
     *
     * @return screenshot in byte array
     */
    public static byte[] take() {
        String screenshotStrategy = Props.get("screenshot.strategy", "raw");

        switch (screenshotStrategy) {
            case "driver":
                return ScreenShooter.takeWithDriver();
            case "raw":
            default:
                return ScreenShooter.takeRaw();
        }
    }
}
