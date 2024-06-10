package main

// this path is interesting - can use gopath or something

/*
#cgo LDFLAGS: -Lgo/ -ltypedb_driver_go_native -framework CoreFoundation
*/
import "C"
import (
	"fmt"
	"os"
	"example/go_wrapper"
)

func openCoreFunc() {
	fmt.Println("hi")
	pwd, err := os.Getwd()
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	fmt.Println(pwd)


	value := 667
	driver_value := typedb_driver.Value_new_long(int64(value))
	fmt.Println(typedb_driver.Value_get_long(driver_value))

}

func main() {
	openCoreFunc()
}
