package foodhunt

/**
  * Created by serg on 31.10.2016.
  */
import foodhunt.HttpRequestor.{Dish, RestoRaunt}
import org.junit.Test;

class HttpRequestorTest {

  @Test
  def testGetAllRestoraunts: Unit ={
    val restos = HttpRequestor.getAllRestoraunts()
     println(restos.size)
  }

  @Test
  def testRestoParse: Unit ={
    val resto = RestoRaunt("https://foodhunt.ee/Restoran/OsmanKebabGrill","Osman Kebab Grill")
    val food = HttpRequestor.getFood(resto)
    println(food.size)
  }

  @Test
  def testDishParse: Unit ={
    val dish = Dish("d",0,"https://foodhunt.ee/Shopping_cart/Add_To_Cart/76/84/4055")
    val food = HttpRequestor.fillDishDetails(dish)
    println(food)
  }

}
