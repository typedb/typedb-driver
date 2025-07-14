import { Then, When } from "@cucumber/cucumber";

Then('wait {int} seconds', async function (seconds: number) {
    await new Promise(f => setTimeout(f, seconds * 1000));
});

When('set time-zone: {word}', async (timezone: string) => {
    process.env.TZ = timezone;
});
