package user

import "C"
import (
	"typedb_driver/go/api/user"
	"typedb_driver/go_wrapper"
)

type UserManagerImpl struct {
	nativeConnection typedb_driver.Connection
	nativeObject typedb_driver.UserManager
}

func NewUserManagerImpl(nativeConnection typedb_driver.Connection) *UserManagerImpl {
	return &UserManagerImpl{
		nativeConnection: nativeConnection,
		nativeObject: typedb_driver.User_manager_new(nativeConnection),
	}

}

func (userMngr *UserManagerImpl) Contains(username string) bool {
	return typedb_driver.Users_contains(userMngr.nativeObject, username)
}

func (userMngr *UserManagerImpl) Create(username, password string) {
	typedb_driver.Users_create(userMngr.nativeObject, username, password)
}

func (userMngr *UserManagerImpl) Delete(username string) {
	typedb_driver.Users_delete(userMngr.nativeObject, username)
}

func (userMngr *UserManagerImpl) Get(username string) typedb_driver.User {
	user_fetched := typedb_driver.Users_get(userMngr.nativeObject, username)
	if user_fetched != nil{
		return NewUserImpl(user_fetched, *userMngr).nativeObject
	} else{
		return nil
	}
}

func (userMngr *UserManagerImpl) PasswordSet(username, password string) {
	typedb_driver.Users_set_password(userMngr.nativeObject, username, password)
}

func (userMngr *UserManagerImpl) GetCurrentUser() user.User {
	//curr := typedb_driver.Users_current_user(this)
	return nil
	//	 return new UserImpl(users_current_user(nativeObject), this);
}


func (userMngr *UserManagerImpl) All() map[string]user.User {


	userSet := make(map[string]user.User)

	//usersIterator := typedb_driver.Users_all(userMngr.nativeObject)
	//typedb_driver.User_iterator_next(usersIterator)
	// TODO implement

	return userSet
}




