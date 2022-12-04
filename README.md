# pdgenruntime
This is the source code for the pdgen document generator.
PDGen is a server side Java or application embedded component, that can be used to generate printable documents.
The documents can either be printed directly or stored in the pdf Format.

PDGen generates the documents through templates, that can either be created programmatically, or through a visual designer, that is available 
from [pdgen.org](http://pdgen.org)

The pdgen-runtime is available under the Gnu Affero license. This means it can be used in closed-source (commercial) projects.
However, when you make extensions and run the extended software on a publicly accesible server, then you must publish your extensions under the same license.

The pdgen-runtime also includes the code for pdgenannotations subproject. This provides a lightweight jar, that you can use to annotate your java classes for processing by pdgen.