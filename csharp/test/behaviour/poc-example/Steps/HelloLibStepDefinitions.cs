using Xunit.Gherkin.Quick;
using System;

namespace Lib.Specs.Steps
{
    [FeatureFile("csharp/test/behaviour/poc-example/Features/HelloLib.feature")]
    public sealed class HelloerStepsDefinition : Feature
    {
        private struct Helper
        {
            public int? NumberValue {get; set;}
            public string StringValue {get; set;}
        };
        private Helper _helper;
        private string _result;

        [Given(@"the name is (.+)")]
        public void GivenNumber(string number)
        {
            _helper.StringValue = number;
        }

        [When(@"they meet Helloer")]
        public void WhenTheyMeetHelloer()
        {
            if (_helper.NumberValue.HasValue)
            {
                _result = Helloer.SayHello(_helper.NumberValue.Value);
            }
            else
            {
                _result = Helloer.SayHello(_helper.StringValue);
            }
        }

        [Then(@"he says '(.+)'")]
        public void ThenTheResultShouldBe(string expectedResult)
        {
            if (_result != expectedResult) {
                throw new InvalidOperationException("Bad" + _result + " vs " + expectedResult);
            }
        }
    }
}