package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import org.mongodb.scala._
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

	val mongoClient = MongoClient("mongodb://localhost:27017")
    val database: MongoDatabase = mongoClient.getDatabase("urlDB")
    val collection: MongoCollection[Document] = database.getCollection("URLs")
	
	def index() = Action { 
		implicit request: Request[AnyContent] => Ok(views.html.index())
	}
  
	def get(shortK: String) = Action { 
		implicit request: Request[AnyContent] => Redirect(Await.result(collection.find(equal("shortKey", shortK)).toFuture(), Duration(10, TimeUnit.SECONDS))(0)("site").asString().getValue()) 
	}
  
	def post() = Action(parse.json) { 
		request =>
		collection.insertOne( Document("site" -> request.body("site").as[String],"shortKey" -> request.body("shortKey").as[String]) ).subscribe(new Observer[InsertOneResult] {		
		override def onNext(result: InsertOneResult): Unit = println(result)
		override def onError(e: Throwable): Unit = println("Failed: " + e.getMessage)
		override def onComplete(): Unit = println("Completed")	
		})	
		Ok("URL added")
    }
  
}
