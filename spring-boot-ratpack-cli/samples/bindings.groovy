package demo
@Grab("io.ratpack:ratpack-session:1.0.0")
import ratpack.session.*

@Service
class MyService {
  String message() { "Hello World" }
}

ratpack {
  bindings {
    module SessionModule
  }
  handlers {
    get { MyService service, Session session ->
      session.data.then { data ->
        data.set('message', service.message())
        render json([message:service.message()])
      }
    }
  }
}