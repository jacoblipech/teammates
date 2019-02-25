package teammates.e2e.pageobjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Represents the home page of the website (i.e., index.jsp).
 */
public class HomePageNew extends AppPageNew {

    @FindBy(id = "btnInstructorLogin")
    private WebElement instructorLoginLink;

    @FindBy(id = "btnStudentLogin")
    private WebElement studentLoginLink;

    public HomePageNew(Browser browser) {
        super(browser);
    }

    @Override
    protected boolean containsExpectedPageContents() {
        return getPageSource().contains("Designed for Simplicity, Flexibility, and Power:");
    }
    /** To implement in the future
    public LoginPageNew clickInstructorLogin() {
        click(instructorLoginLink);
        waitForPageToLoad();
        String pageSource = getPageSource();
        if (InstructorHomePage.containsExpectedPageContents(pageSource)) {
            //already logged in. We need to logout because the return type of
            //  this method is a LoginPageNew
            logout();
            click(instructorLoginLink);
            waitForPageToLoad();
        }
        return createCorrectLoginPageType(browser);

    }
    */

    public LoginPageNew clickStudentLogin() {
        click(studentLoginLink);
        waitForPageToLoad();
        String pageSource = getPageSource();
        if (StudentHomePageNew.containsExpectedPageContents(pageSource)) {
            //already logged in. We need to logout because the return type of
            //  this method is a LoginPageNew
            logout();
            click(studentLoginLink);
            waitForPageToLoad();
        }
        return createCorrectLoginPageType(browser);
    }

}
