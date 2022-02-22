
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._


class jPetStore extends Simulation {

  private val httpProtocol = http
    .baseUrl("https://petstore.octoperf.com")
    .inferHtmlResources()
    .silentResources
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("pt-BR,pt;q=0.9")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")
  
  
  private val headers_2 = Map(
  		"Origin" -> "https://petstore.octoperf.com"
  )

  val animals = csv("animals.csv").circular()

  private val scn = scenario("jPetStore")
  .repeat (3) {
    exec(
      http("request_0")
        .get("/")
        
    )
    .pause(3)
    .exec(
      http("request_1")
        .get("/actions/Catalog.action")
        .check(regex("""name="_sourcePage" value=(.+?)"""").saveAs("sourcePage"),
          regex("""name="__fp" value=(.+?)"""").saveAs("fp")))
    .pause(8)

    .feed(animals)
    .exec(
      http("request_2")
        .post("/actions/Catalog.action")
        .headers(headers_2)
        .formParam("keyword", "#{animal}")
        .formParam("searchProducts", "Search")
        .formParam("_sourcePage", "#{sourcePage}")
        .formParam("__fp", "${fp}")
        .check(regex("""/actions/Catalog\.action\?viewProduct=\&amp;productId=(.+?)"""").saveAs("productId"))
    )
    .pause(9)
    .exec(
      http("request_3")
        .get("/actions/Catalog.action?viewProduct=&productId=#{productId}")
        .check(substring("""<h2>#{animal}</h2>"""))
    )
  }

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
  .assertions(details("request_0").responseTime.max.lt(50),global.failedRequests.percent.is(0))
}
