GIT = git
MAKE = make

# echo v0.1 to get it started
VERSION := $(shell git describe --tags || echo "v0.1" | sed -e 's/^v//;s/^[^0-9]*//;s/-/./;s/\(.*\)-g/\1-/')

.PHONY: .filelist
.filelist:
	@set -e ; submodules=`$(GIT) submodule foreach --quiet 'echo $$path'`; \
		$(GIT) ls-files | \
		grep -vxF -e "$$submodules" | \
		grep -v "gitignore\|gitmodules" > .filelist
	@$(GIT) submodule foreach --quiet 'git ls-files | sed -e "s,^,$$path/,"' | \
		grep -v "gitignore\|gitmodules" >> .filelist
	@[ -s .filelist ] # assert there is something in .filelist now
	@echo .filelist >> .filelist ; \
		echo "./.filelist updated."


tgz:
	test -s .filelist
	@if [ ! -d .git ]; then \
		echo >&2 "Not a git directory!"; exit 1; \
	fi; \
	tar --transform="s,^,linstor-server-$(VERSION)/,"          \
	   --owner=0 --group=0 -czf - -T .filelist > linstor-server-$(VERSION).tar.gz

# we cannot use 'git submodule foreach':
# foreach only works if submodule already checked out
.PHONY: check-submods
check-submods:
	@if test -d .git && test -s .gitmodules; then \
		for d in `grep "^\[submodule" .gitmodules | cut -f2 -d'"'`; do \
			if [ ! "`ls -A $$d`" ]; then \
				git submodule init; \
				git submodule update; \
				break; \
			fi; \
		done; \
	fi

prepare_release: tarball
debrelease: tarball

.PHONY: check-all-committed
check-all-committed:
	if ! tmp=$$(git diff --name-status HEAD 2>&1) || test -n "$$tmp" ; then \
		echo >&2 "$$tmp"; echo >&2 "Uncommitted changes"; exit 1; \
	fi

.PHONY: check_changelogs_up2date
check_changelogs_up2date:
	grep "^version" build.gradle | grep $(VERSION)

tarball: check-all-committed check_changelogs_up2date check-submods .filelist
	$(MAKE) tgz


