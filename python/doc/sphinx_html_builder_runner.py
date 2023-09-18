import sys

from sphinx.cmd.build import main
from sphinx.ext import apidoc


if __name__ == '__main__':
    apidoc.main(["-o", "python/doc/source", "python/driver-package/typedb"])
    sys.exit(main(["-M", "html", "python/doc/source", "python/doc/build"]))
