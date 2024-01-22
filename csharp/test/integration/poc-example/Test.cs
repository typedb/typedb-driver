using NUnit.Framework;

namespace Lib.IntegrationTests
{
    [TestFixture]
    public class Helloer_Says
    {
//        [SetUp]
//        public void SetUp()
//        {
//            _helloer = new Helloer(); // not needed as Helloer is static
//        }

        [Test]
        public void HelloToWorld()
        {
            var result = Helloer.SayHello("world");

            Assert.AreEqual(result, "Hello, world!");
        }

        [Test]
        public void HelloToNumber()
        {
            var result = Helloer.SayHello(34);

            Assert.AreEqual(result, "Hello, 34!");
        }

        [Test]
        public void HelloToJohn()
        {
            var result = Helloer.SayHello("John");

            Assert.AreEqual(result, "What's up, John!");
        }
    }
}