# This Makefile has only been tested on linux.  It uses
# MinGW32 to cross-compile for windows.  To install and
# configure MinGW32 on linux, see http://www.mingw.org


# This is where the mingw32 compiler exists in Ubuntu 8.04.
# Your compiler location may vary.
WIN32_CC=/usr/bin/i686-w64-mingw32-gcc

CFLAGS=-Wall -pedantic -O2
SRCDIR=nailgun-client/c
TARGETDIR=nailgun-client/target
PREFIX=/usr/local

ng: ${SRCDIR}/ng.c
	@echo "Building ng client. To build a Windows binary, type 'make ng.exe'"
	mkdir -p ${TARGETDIR}
	${CC} $(CPPFLAGS) $(CFLAGS) $(LDFLAGS) -o ${TARGETDIR}/ng ${SRCDIR}/ng.c

install: ng
	install -d ${PREFIX}/bin
	install ${TARGETDIR}/ng ${PREFIX}/bin
	
ng.exe: ${SRCDIR}/ng.c
	mkdir -p ${TARGETDIR}
	${WIN32_CC} -o ${TARGETDIR}/ng.exe ${SRCDIR}/ng.c -lwsock32 -O3 ${CFLAGS}
# any idea why the command line is so sensitive to the order of
# the arguments?  If CFLAGS is at the beginning, it won't link.
	
clean:
	@echo ""
	@echo "If you have a Windows binary, 'make clean' won't delete it."
	@echo "You must remove this manually.  Most users won't have MinGW"
	@echo "installed - so I'd rather not delete something you can't rebuild."
	@echo ""
	rm -f ${TARGETDIR}/ng
#	rm -f ${TARGETDIR}/ng.exe
