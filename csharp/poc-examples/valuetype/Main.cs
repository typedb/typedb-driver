using com.vaticle.typedb.driver.pinvoke;

namespace com.vaticle.typedb.driver.pinvoke.TypeDB
{
    public static class ValueTyper
    {
        public static int Main()
        {
//            Options option = new Options();
            typedb_driver driver = new typedb_driver();
//            driver.options_has_parallel(option);
            ValueType val = new ValueType();

            System.Console.WriteLine("Hello!" + val);

            return 0;
        }
    }
}
