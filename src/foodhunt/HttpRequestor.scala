package foodhunt


import java.net.URL
import javax.net.ssl.HttpsURLConnection

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

import scala.sys.process.processInternal.InputStream


/**
  * Created by serg on 31.10.2016.
  */
object HttpRequestor {

  case class RestoRaunt(href: String, name: String)
  case class Dish(name: String, price: Double, href: String)
  case class DishDetails(dish: Dish,details : List[(String, String)] = Nil)

  def getAllRestoraunts(): List[RestoRaunt] = {
    val inStream =createConnection("https://foodhunt.ee/Restoranid/Harjumaa/Kesklinn")
    try {
      val browser = JsoupBrowser()
      val doc = browser.parseInputStream(inStream)
      val h3: List[Element] = doc >> elementList(".restaurant_name")
      return h3.map(rest => RestoRaunt(rest >> attr("href")("a"), rest >> text("span")))
    } finally {
      inStream.close;
    }
    Nil
  }

  def getFood(resto: RestoRaunt): List[Dish] = {
    val inStream = createConnection(resto.href)
    try {
      val browser = JsoupBrowser()
      val doc = browser.parseInputStream(inStream)
      val container: List[Element] = doc >> elementList(".col-md-8")

      val foodContainer = container.head >> elementList("div")
      val foodDivs = foodContainer.filter(el => el.attrs.find(t => t._1 == "id" && t._2.startsWith("dish")).isDefined)
      return foodDivs.map(getDishDesc(_))
    } finally {
      inStream.close;
    }
    Nil
  }

  private def getDishDesc(element: Element): Dish = {
    val divs = element >> elementList("div")
    val dishName = divs
      .filter(div => div.attr("class").endsWith("description"))
      .map(div => div >> text("h2"))
      .head
    val dishUrl = element >> attr("href")("div")
    val dishPrice = divs
      .filter(div => div.attr("class").endsWith("price"))
      .map(div => div >> text("span"))
      .head
      .replace("€", "")
      .replace("al.", "")
      .trim
    Dish(dishName, dishPrice.toDouble,dishUrl)
  }

  def fillDishDetails(dish: Dish) : DishDetails = {
    val inStream = createConnection(dish.href)
    try {
      val browser = JsoupBrowser()
      val doc = browser.parseInputStream(inStream)
      val container: List[Element] = doc >> elementList(".checkbox_awesome-success")
      val details = container.map(el=> {
        val price = el >> attr("data-price")("input")
        val name = el >> text("label")
        (name, price)
      })
      return DishDetails(dish,details)
    } finally {
      inStream.close;
    }
    DishDetails(dish)
  }



  private def createConnection(href: String): InputStream = {
    val url = new URL(href);
    val con = url.openConnection().asInstanceOf[HttpsURLConnection];
    con.setConnectTimeout(3000)
    con.getInputStream
  }

}
