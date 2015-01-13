import ratpack.jackson.JacksonModule

@Service
class MyService {
  String message() { "Hello World" }
}

ratpack {
  bindings {
	  def module = new JacksonModule()
	  module.configure {
		  def config -> config.prettyPrint(false)
	  }
	  add(module)
  }
  handlers {
    get { MyService service ->
      render json([msg:service.message()])
    }
  }
}