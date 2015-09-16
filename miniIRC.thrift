namespace java if4031

typedef i32 int

service miniIRC {
	int join(1: string username, 2: string channelname),
	int regUser(1: string username),
	int leave(1: string username, 2: string channelname),
	int exit(1: string username),
	int message(1: string username, 2: string channelname, 3: string msg),
	string regularUpdate(1: string username)
}