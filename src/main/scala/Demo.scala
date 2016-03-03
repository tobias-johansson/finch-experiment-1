import com.twitter.finagle.{Http, http}
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import io.circe.jackson._

object Main extends App {

	case class Div(a: Int, b: Int)
	case class Res(result: Int, extra: Headers)

  case class Headers(headers: Map[String, String])

	val divByBody:   Endpoint[Div] = post(body).as[Div]

  val div: Endpoint[Res] = divByBody { d: Div =>
  	println(s"${d.a} / ${d.b}")
  	val ser = Http.client.withTls("httpbin.org").newService("httpbin.org:443")
		val req = http.Request("/headers")

  	ser(req) map { rep =>
      decode[Headers](rep.contentString).fold(
        err => InternalServerError(err),
        hdr => Ok(Res(d.a / d.b, hdr))
      )
    }
  } handle {
    case e: ArithmeticException => BadRequest(e)
  }

	implicit val encodeException: Encoder[Exception] = Encoder.instance(e =>
	  Json.obj(
	    "type"    -> Json.string(e.getClass.getSimpleName),
	    "message" -> Json.string(e.getMessage)
	  )
	)

  val service = (
		"div" / div
	).toService

  Await.ready(Http.server.serve(":8081", service))
}
