package lib

var testJson = `{"page": 1, "fruits": ["apple", "peach"]}`
var complexJson = `
	{
	"result_count": 1,
	"results": [
		{
		"enumeration_type": "NPI-1",
		"number": 1245319599,
		"last_updated_epoch": 1351814400,
		"created_epoch": 1162771200,
		"basic": {
			"name_prefix": "DR.",
			"first_name": "LAURA",
			"last_name": "SAMPLE",
			"middle_name": "TURTZO",
			"credential": "MD",
			"sole_proprietor": "NO",
			"gender": "F",
			"enumeration_date": "2006-11-06",
			"last_updated": "2012-11-02",
			"status": "A",
			"name": "SAMPLE LAURA"
		},
		"other_names": [],
		"addresses": [
			{
			"country_code": "US",
			"country_name": "United States",
			"address_purpose": "LOCATION",
			"address_type": "DOM",
			"address_1": "1080 FIRST COLONIAL RD",
			"address_2": "SUITE 200",
			"city": "VIRGINIA BEACH",
			"state": "VA",
			"postal_code": "234542406",
			"telephone_number": "757-395-6070",
			"fax_number": "757-395-6381"
			},
			{
			"country_code": "US",
			"country_name": "United States",
			"address_purpose": "MAILING",
			"address_type": "DOM",
			"address_1": "1080 FIRST COLONIAL RD",
			"address_2": "SUITE 200",
			"city": "VIRGINIA BEACH",
			"state": "VA",
			"postal_code": "234542406",
			"telephone_number": "757-395-6070",
			"fax_number": "757-395-6381"
			}
		],
		"taxonomies": [
			{
			"code": "207Q00000X",
			"desc": "Family Medicine",
			"primary": true,
			"state": "VA",
			"license": "0101244988"
			}
		],
		"identifiers": []
		}
	]
	}
	`

func SayHello() string {
	return "Hello, World!"
}

func ReturnJson() []byte {
	return []byte(complexJson)
}
