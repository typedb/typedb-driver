package main

import "C"
import (
	"fmt"
	"os"
	"typedb_driver/go_wrapper"
)

func openCoreFunc() {
	pwd, err := os.Getwd()
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	fmt.Println(pwd)

	// these should be implemented using the dylib, so if all is well these should run.
	value := 667
	driverValue := typedb_driver.Value_new_long(int64(value))
	fmt.Println(typedb_driver.Value_get_long(driverValue))

}

func main() {
	openCoreFunc()
}
