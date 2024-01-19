using System;

namespace Lib {
  public static class Helloer {
    public static string SayHello(int number)
    {
        return SayHello(number.ToString());
    }

    public static string SayHello(string name)
    {
        string hello = "Hello, ";

        if (name == "John") {
            hello = "What's up, ";
        }

        return hello + name + "!";
    }
  }
}