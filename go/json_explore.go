package main

import (
	"encoding/json"
	"fmt"
	lib "hello_lib_path"
)

// we use Marshal to encode, UnMarshal to decode

func main() {
	unmarshalComplex(lib.ReturnJson())
}

func unmarshalComplex(j []byte) {

	fmt.Printf("json, %s\n", j)

	// get json as a map with interface{}
	jsonMap := make(map[string](interface{}))
	err := json.Unmarshal([]byte(j), &jsonMap)
	if err != nil {
		fmt.Printf("ERROR: fail to unmarshal json, %s\n", err.Error())
	}
	fmt.Printf("jsonMap, %s\n", jsonMap)

	// results list
	resultList := jsonMap["results"].([]interface{})
	fmt.Printf("resultList, %s\n", resultList)

	// first element of result list as a map
	resultMap := resultList[0].(map[string]interface{})
	fmt.Printf("resultMap, %s\n", resultMap)
	fmt.Printf("number, %f\n", resultMap["number"].(float64))

	// get basic
	basicMap := resultMap["basic"].(map[string]interface{})
	fmt.Printf("basicMap, %s\n", basicMap)
	fmt.Printf("first name, %s\n", basicMap["first_name"])
}
