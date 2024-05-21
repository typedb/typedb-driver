package lib

import (
	"hello_lib_path"
	"testing"
)

func TestHello(t *testing.T) {
	expected := "Hello, World!"
	if got := lib.SayHello(); got != expected {
		t.Errorf(" = %v, want %v", got, expected)
	}
}
