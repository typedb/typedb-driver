using com.vaticle.typedb.driver.pinvoke;
using System;
using System.Reflection;
using System.Runtime.InteropServices;
using System.IO;

namespace com.vaticle.typedb.driver.pinvoke.TypeDB
{
    public static class ValueTyper
    {

        public static int Main()
        {
            System.Console.WriteLine("Begin!");
            typedb_driver driver = new typedb_driver();
            com.vaticle.typedb.driver.pinvoke.ValueType val = com.vaticle.typedb.driver.pinvoke.ValueType.Boolean;
            var concept = typedb_driver.value_new_long(50);
            System.Console.WriteLine("Hello!" + val + typedb_driver.value_get_long(concept));
            return 0;
        }
    }
}
