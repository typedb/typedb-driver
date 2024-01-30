using Xunit.Gherkin.Quick;
using System;

using com.vaticle.typedb.driver;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Database
{
    [FeatureFile("csharp/test/behaviour/connection/database/connection/database.feature")]
    public sealed class DatabaseTest : Feature
    {
        private string _result;

        [Given(@"the name is (.+)")]
        public void GivenNumber(string number)
        {
            Console.WriteLine("Fine");
        }

        [When(@"th")]
        public void When()
        {
            Console.WriteLine("When");
        }

        [Then(@"aa'(.+)'")]
        public void ThenAa(string expectedResult)
        {
            Console.WriteLine("Res" + expectedResult);
        }
    }
}
