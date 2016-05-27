
package hcm.common;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DriverCommand;

/**
 * An extension of the default <tt>FirefoxDriver</tt> class but with support for
 * taking screenshots.
 * 
 * @author phongtran
 */
public class ExtendedFirefoxDriver extends FirefoxDriver implements TakesScreenshot
{
    /**
     * Constructor.
     * 
     * @param capabilities
     */
    public ExtendedFirefoxDriver(Capabilities capabilities)
    {
        super(capabilities);
    }

    /**
     * Retrieves the screenshot data as a byte array.
     * 
     * @param target The output type.
     * @return The screenshot data as a byte array.
     * @throws WebDriverException
     */
    public byte[] getScreenshot(OutputType<byte[]> target) throws WebDriverException
    {
        String base64Str = execute(DriverCommand.SCREENSHOT).getValue().toString();
        return target.convertFromBase64Png(base64Str);
    }
}
