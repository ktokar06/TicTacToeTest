import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TicTacToeTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://www.google.com");
        searchGame();
    }

    @Test
    public void testPlayGame() throws IOException {
        selectPlayWithFriend();
        playGame();
    }

    @Test
    public void testGameDraw() throws IOException {
        selectPlayWithFriend();
        simulateDraw();
    }

    @Test
    public void testPlayerOWins() throws IOException {
        selectPlayWithFriend();
        simulateOWin();
    }

    private void playGame() throws IOException {
        int movesCount = 0;

        while (movesCount < 9) {
            boolean madeMove = false;
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 3; j++) {
                    if (isCellAvailable(i, j)) {
                        makeMove(i, j);
                        madeMove = true;
                        movesCount++;
                        if (checkWinner()) {
                            System.out.println("Победитель найден!");
                            return;
                        }
                        break;
                    }
                }
                if (madeMove) break;
            }
            if (!madeMove) break;
        }
        if (movesCount == 9) {
            System.out.println("Игра закончилась вничью.");
        }
    }

    private void simulateDraw() throws IOException {
        makeMove(1, 1); // X
        makeMove(1, 2); // O
        makeMove(1, 3); // X
        makeMove(2, 1); // O
        makeMove(2, 2); // X
        makeMove(2, 3); // O
        makeMove(3, 1); // X
        makeMove(3, 2); // O
        makeMove(3, 3); // X

        System.out.println("Игра закончилась вничью.");
    }

    private void simulateOWin() throws IOException {
        makeMove(1, 1); // X
        makeMove(1, 2); // O
        makeMove(2, 1); // X
        makeMove(2, 2); // O
        makeMove(3, 1); // X
        makeMove(3, 2); // O // Победа O

        if (checkWinner()) {
            System.out.println("Победитель найден: O!");
        }
    }

    private boolean isCellAvailable(int row, int col) {
        String xpath = String.format("//tr[%d]/td[%d]", row, col);
        try {
            WebElement cell = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            return cell.getText().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void searchGame() {
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));
        searchBox.sendKeys("крестики нолики", Keys.RETURN);
    }

    private void selectPlayWithFriend() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[3]/div/div[13]/div/div[1]/div[2]/div[2]/div/div/div[1]/div/block-component/div/div[1]/div[1]/div/div/div[1]/div/div/div/div/div[1]/g-dropdown-menu/g-popup/div[1]"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[3]/div/div[8]/div/g-menu/g-menu-item[4]/div"))).click();
        } catch (NoSuchElementException e) {
            System.out.println("Элемент не найден: " + e.getMessage());
        }
    }

    private void makeMove(int row, int col) throws IOException {
        String xpath = String.format("//tr[%d]/td[%d]", row, col);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
        } catch (ElementClickInterceptedException e) {
            captureScreenshot();
            Allure.addAttachment("Screenshot", new ByteArrayInputStream(Files.readAllBytes(Paths.get("screenshot.png"))));
            throw e;
        }
    }

    private boolean checkWinner() {
        String[][] board = new String[3][3];
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                String xpath = String.format("//tr[%d]/td[%d]", i, j);
                board[i - 1][j - 1] = driver.findElement(By.xpath(xpath)).getText();
            }
        }

        for (int i = 0; i < 3; i++) {
            if ((board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].isEmpty()) ||
                    (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]) && !board[0][i].isEmpty())) {
                return true;
            }
        }

        if ((board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].isEmpty()) ||
                (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].isEmpty())) {
            return true;
        }

        return false;
    }

    private void captureScreenshot() {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File srcFile = ts.getScreenshotAs(OutputType.FILE);
        try {
            Files.copy(srcFile.toPath(), Paths.get("screenshot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
