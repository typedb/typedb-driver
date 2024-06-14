package Connection

import "C"
import (
	"typedb_driver/go_wrapper"
)

// MyDriver struct
type MyDriver struct {
	open           bool
	dbManager      DatabaseManager
	currentUser    User
	userManager    UserManager
}

// NewMyDriver function to initialize MyDriver with necessary dependencies
func NewMyDriver(dbManager DatabaseManager, user User, userManager UserManager) *MyDriver {
	return &MyDriver{
		open:        true,
		dbManager:   dbManager,
		currentUser: user,
		userManager: userManager,
	}
}

func (d *MyDriver) IsOpen() bool {
	return typedb_driver.Connection_is_open(nativeObject)

}

func (d *MyDriver) Databases() DatabaseManager {
	return d.dbManager
}

func (d *MyDriver) Close() {
	d.open = false
	// Add logic to close connections or perform other cleanup tasks
}

func (d *MyDriver) User() User {
	return d.currentUser
}

func (d *MyDriver) Users() UserManager {
	return d.userManager
}
