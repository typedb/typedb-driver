import { Then, When } from "@cucumber/cucumber";

Then('wait {int} seconds', async function (seconds: number) {
    await new Promise(f => setTimeout(f, seconds * 1000));
});

When('set time-zone: {Timezone}', async (timezone: string) => {
    process.env.TZ = timezone;
});
