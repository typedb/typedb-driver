package main

// this path is interesting - can use gopath or something

/*
#cgo LDFLAGS: -Lswigdir -ltypedb_driver_go_native -framework CoreFoundation
*/
import "C"
import (
	"fmt"
	//"go/swigdir" we actually need to import a go_lib which contains the path to the go wrapper
)

func openCoreFunc() {
	fmt.Println("hi")

	value := 667
	driver_value := typedb_driver.Value_new_long(int64(value))
	fmt.Println(typedb_driver.Value_get_long(driver_value))
}

func main() {
	openCoreFunc()
}
