package user

import (
	"typedb_driver/go_wrapper"
)

type UserImpl struct {
	users UserManagerImpl
	nativeObject typedb_driver.User
}

func NewUserImpl(user typedb_driver.User, users UserManagerImpl,) *UserImpl {
	return &UserImpl{
		users:        users,
		nativeObject: user,
	}
}

func (u *UserImpl) username() string {
	return typedb_driver.User_get_username(u.nativeObject)
}

func (u *UserImpl) passwordExpirySeconds() int64 {
	return typedb_driver.User_get_password_expiry_seconds(u.nativeObject)
}

func (u *UserImpl) passwordUpdate(passwordOld string, passwordNew string)  {
	typedb_driver.User_password_update(u.nativeObject, u.users.nativeObject, passwordOld, passwordNew)
}
