using System;
using System.IO;
using System.Reflection;
using System.Threading;
using Xunit.Runners;

// TODO: Remove all or reduce WriteLines to Console if we don't want to see them.
namespace Vaticle.Typedb.Driver.Test.TestRunner
{
    class Program
    {
        // We use consoleLock because messages can arrive in parallel, so we want to make sure we get
        // consistent console output.
        static object consoleLock = new object();

        // Use an event to know when we're done.
        static ManualResetEvent finished = new ManualResetEvent(false);

        // Start out assuming success; we'll set this to 1 if we get a failed test.
        static int result = 0;

        static int Main(string[] args)
        {
            var testAssembly = Assembly.GetExecutingAssembly().Location;
            Console.WriteLine(testAssembly);
            var typeName = args.Length == 1 ? args[0] : null;

            using (var runner = AssemblyRunner.WithoutAppDomain(testAssembly))
            {
                runner.OnDiscoveryComplete = OnDiscoveryComplete;
                runner.OnExecutionComplete = OnExecutionComplete;
                runner.OnTestFailed = OnTestFailed;
                runner.OnTestSkipped = OnTestSkipped;
                runner.OnTestPassed = OnTestPassed;
                runner.OnTestStarting = OnTestStarting;

                Console.WriteLine("Discovering...");
                runner.Start(typeName);

                finished.WaitOne();
                finished.Dispose();

                return result;
            }
        }

        static void OnDiscoveryComplete(DiscoveryCompleteInfo info)
        {
            lock (consoleLock)
            {
                Console.WriteLine($"Running {info.TestCasesToRun} of {info.TestCasesDiscovered} tests...");
            }
        }

        static void OnTestStarting(TestStartingInfo info)
        {
            lock (consoleLock)
            {
                Console.WriteLine(
                    $"Starting test {info.TestDisplayName}...");
            }
        }

        static void OnTestPassed(TestPassedInfo info)
        {
            lock (consoleLock)
            {
                Console.WriteLine(
                    $"[PASSED] {info.TestDisplayName}");
            }
        }

        static void OnExecutionComplete(ExecutionCompleteInfo info)
        {
            lock (consoleLock)
            {
                Console.WriteLine(
                    $"Finished: {info.TotalTests} tests in {Math.Round(info.ExecutionTime, 3)}s "
                        + $"({info.TestsFailed} failed, {info.TestsSkipped} skipped)");
            }

            finished.Set();
        }

        static void OnTestFailed(TestFailedInfo info)
        {
            lock (consoleLock)
            {
                Console.ForegroundColor = ConsoleColor.Red;

                Console.WriteLine("[FAIL] {0}: {1}", info.TestDisplayName, info.ExceptionMessage);
                if (info.ExceptionStackTrace != null)
                {
                    Console.WriteLine(info.ExceptionStackTrace);
                }

                Console.ResetColor();
            }

            result = 1;
        }

        static void OnTestSkipped(TestSkippedInfo info)
        {
            lock (consoleLock)
            {
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine("[SKIP] {0}: {1}", info.TestDisplayName, info.SkipReason);
                Console.ResetColor();
            }
        }
    }
}
