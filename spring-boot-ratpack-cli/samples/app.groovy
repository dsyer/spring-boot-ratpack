@Service
class MyService {
  String message() { "Hello World" }
}

ratpack {
  handlers {
    get { MyService service ->
      render json([msg:service.message()])
    }
  }
}