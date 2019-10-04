# Please do not convert this Makefile to use CRLF line endings.
# A simple Makefile based build system for Unix and Unix-like OSes.
# May also work in Windows, but this is totally untested.
# Should work in Mingw/MSYS and Cygwin just fine, though.
# (Avoids a dependency on an IDE)
JAVAC=javac
JAR=jar

ifeq ($(OS),Windows_NT)
    RM = cmd //C del //Q //F
    RRM = cmd //C rmdir //Q //S
    # in Windows/DOS, mkdir returns an error code if the dir already exists,
    # with no way to override that behavior (e.g. Unix mkdir -p).
    # So we do this ugly thing
    MKDIR = mkdir $(subst /,\,$(1)) > nul 2>&1 || (exit 0)
else
    RM = rm -f
    RRM = rm -r -f
    MKDIR = mkdir -p $(1)
endif

jtitsed.jar: bin bin/org/csdgn/titsed/ui/strings.properties
	$(JAR) cmf jtitsed.mf jtitsed.jar jtitsed.mf -C src resources -C bin org

bin:
	$(call MKDIR,bin)
	$(JAVAC) -d bin -sourcepath src src/org/csdgn/titsed/*.java src/org/csdgn/titsed/ui/*.java src/org/csdgn/titsed/model/*.java src/org/csdgn/amf3/*.java src/org/csdgn/maru/*.java src/org/csdgn/maru/swing/*.java

bin/org/csdgn/titsed/ui/strings.properties: bin
	ln -sf ../../../../../src/org/csdgn/titsed/ui/strings.properties bin/org/csdgn/titsed/ui/strings.properties

.PHONY: clean

clean:
	$(RRM) bin
	$(RM) jtitsed.jar

