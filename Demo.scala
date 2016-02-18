import com.twitter.finagle.{Http, http}
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import io.circe.{Encoder, Json}

object Main extends App {

	case class Div(a: Int, b: Int)
	case class Res(result: Int, extra: String)

	val divByPath:   Endpoint[Div] = get(int :: int).as[Div]
	val divByParams: Endpoint[Div] = get(param("a").as[Int] :: param("b").as[Int]).as[Div]
	val divByBody:   Endpoint[Div] = post(body).as[Div]

  val div: Endpoint[Res] = divByBody { d: Div =>
  	println(s"${d.a} / ${d.b}")
  	val s = Http.newService("httpbin.org:443")
  	val r = s(http.Request("/user-agent"))
  	r.map (rep => Ok(Res(d.a / d.b, rep.contentString)))
  } handle {
    case e: ArithmeticException => BadRequest(e)
  }

	implicit val encodeException: Encoder[Exception] = Encoder.instance(e =>
	  Json.obj(
	    "type"    -> Json.string(e.getClass.getSimpleName),
	    "message" -> Json.string(e.getMessage)
	  )
	)

  val service = ("div" / div).toService

  Await.ready(Http.server.serve(":8081", service))
}