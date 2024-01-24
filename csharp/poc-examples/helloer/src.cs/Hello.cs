using System;
using Lib;

namespace Hello {
  public static class Program {
    public static void Main() {
      Console.WriteLine(Helloer.SayHello("world"));
      Console.WriteLine(Helloer.SayHello("John"));
      Console.WriteLine(Helloer.SayHello(556));
    }
  }
}


using System.Collections;