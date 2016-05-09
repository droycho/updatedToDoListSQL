import org.sql2o.*;
import org.junit.*;
import org.fluentlenium.adapter.FluentTest;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.*;
import static org.junit.Assert.*;

public class AppTest extends FluentTest {
  public WebDriver webDriver = new HtmlUnitDriver();

  @Override
  public WebDriver getDefaultDriver() {
    return webDriver;
  }

  @ClassRule
  public static ServerRule server = new ServerRule();

  @Before
  public void setUp() {
    DB.sql2o = new Sql2o("jdbc:postgresql://localhost:5432/to_do_test", null, null);
  }

  @After
  public void tearDown() {
    try(Connection con = DB.sql2o.open()) {
      String deleteTasksQuery = "DELETE FROM tasks *;";
      String deleteCategoriesQuery = "DELETE FROM categories *;";
      String deleteCategoriesTasksQuery = "DELETE FROM categories_tasks *;";
      con.createQuery(deleteTasksQuery).executeUpdate();
      con.createQuery(deleteCategoriesQuery).executeUpdate();
      con.createQuery(deleteCategoriesTasksQuery).executeUpdate();
    }
  }

  @Test
  public void rootTest() {
    goTo("http://localhost:4567/");
    assertThat(pageSource()).contains("Todo list!");
  }

  @Test
  public void categoryIsCreatedTest() {
    goTo("http://localhost:4567/");
    click("a", withText("Categories"));
    fill("#name").with("Household chores");
    submit(".btn");
    assertThat(pageSource()).contains("Household chores");
  }

  @Test
  public void taskIsCreatedTest() {
    goTo("http://localhost:4567/");
    click("a", withText("Tasks"));
    fill("#description").with("Mow the lawn");
    submit(".btn");
    assertThat(pageSource()).contains("Mow the lawn");
  }

  @Test
  public void categoryShowPageDisplaysName() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    String url = String.format("http://localhost:4567/categories/%d", testCategory.getId());
    goTo(url);
    assertThat(pageSource()).contains("Household chores");
  }

  @Test
  public void taskShowPageDisplaysDescription() {
    Task testTask = new Task("Mow the lawn", "2016-05-25");
    testTask.save();
    String url = String.format("http://localhost:4567/tasks/%d", testTask.getId());
    goTo(url);
    assertThat(pageSource()).contains("Mow the lawn");
  }

  @Test
  public void taskIsAddedToCategory() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Task testTask = new Task("Mow the lawn", "2016-05-25");
    testTask.save();
    String url = String.format("http://localhost:4567/categories/%d", testCategory.getId());
    goTo(url);
    fillSelect("#task_id").withText("Mow the lawn");
    submit(".btn");
    assertThat(pageSource()).contains("<li>");
    assertThat(pageSource()).contains("Mow the lawn");
  }

  @Test
  public void categoryIsAddedToTask() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Task testTask = new Task("Mow the lawn", "2016-05-25");
    testTask.save();
    String url = String.format("http://localhost:4567/tasks/%d", testTask.getId());
    goTo(url);
    fillSelect("#category_id").withText("Household chores");
    submit(".btn");
    assertThat(pageSource()).contains("<li>");
    assertThat(pageSource()).contains("Household chores");
  }

}
