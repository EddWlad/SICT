import { Role } from "./role.model";
import { User } from "./user.model";

export interface UserListRoleDTOI {
  user: User;
  roles: Role[];
}
