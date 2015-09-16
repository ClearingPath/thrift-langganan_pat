namespace java if4031

typedef i32 int

service miniIRC {
	int join(1: string channelname),
	int regUser(1: string username),
	int leave(1: string channelname, 2: string username),
	int exit(),
	int message(1: string msg),
	int regularUpdate()
}