@Service
class MyService {
  String message() { "Hello World" }
}

ratpack {
  serverConfig { builder ->
    builder.development(true);
  }
  handlers {
    get { MyService service ->
      render json([msg:service.message()])
    }
  }
}