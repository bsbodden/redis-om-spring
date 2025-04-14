# Redis OM Spring Documentation

Documentation site for Redis OM Spring, based on [Antora](https://antora.org/).

## Building the Documentation

### Prerequisites

- Node.js 16+
- npm
- Java 17+ (if using Maven)

### Installation

./mvnw compile -pl docs

npx antora -v

npx antora antora-playbook.yml

```bash
npm install
```

### Building the Site

#### Using npm

```bash
npm run build -- --to-dir=target/site
```

The documentation will be built in the `target/site` directory (same location as the Maven build).

#### Using Maven

You can also build the site using Maven:

```bash
mvn clean compile
```

### Viewing the Documentation

After building, you can view the documentation by opening any of these files in your browser:

- `target/site/index.html` - Documentation homepage
- `target/site/redis-om-spring/current/index.html` - Current version documentation
- `target/site/redis-om-spring/current/overview.html` - Overview page

## Content Structure

The documentation content is organized as follows:

- `content/` - Source content
  - `antora.yml` - Content manifest
  - `modules/` - Documentation modules
    - `ROOT/` - Main module
      - `nav.adoc` - Navigation sidebar
      - `pages/` - Documentation pages
      - `images/` - Images used in documentation

## Development

### Docker Setup

You can also serve the documentation using Docker after building:

```bash
# First build the site
npm run build
# or 
mvn clean compile

# Then serve with Docker
docker-compose up
```

The documentation will be available at http://localhost:8000.

Note: The Docker setup uses the `target/site` directory, so make sure you've built the documentation first.

### Adding New Content

To add a new page:

1. Create a new AsciiDoc file in `content/modules/ROOT/pages/`
2. Add the page to the navigation in `content/modules/ROOT/nav.adoc`
3. Rebuild the site

## Current Content Status

- Overview and general information pages are complete
- AI and Vector Search pages are well-developed
- Many other sections require completion

## Feature Documentation Status

- [x] Overview/Introduction
- [x] Why Redis OM Spring
- [x] Setup/Configuration
- [x] Vector Search (including Azure OpenAI)
- [x] AI Integration
- [x] Quick Start Example
- [ ] Core Concepts (partially complete)
- [ ] Redis Hashes (partially complete)
- [ ] Redis JSON (partially complete)
- [ ] Search & Indexing (partially complete)
- [ ] Entity Streams (partially complete)
- [ ] Aggregations (partially complete)
- [ ] Probabilistic Data Structures
- [ ] Testing & Development

## Future Improvements

- Complete all reference documentation pages
- Add diagrams to explain complex concepts
- Include more examples from demo applications
- Standardize page structure and formatting
- Add navigation breadcrumbs
- Integrate automated API documentation

## Troubleshooting

If you encounter any issues:

1. Make sure you have the correct Node.js version (16+)
2. Clear the cache with `rm -rf .cache/`
3. For Maven builds, run `mvn clean` first
4. Ensure the output directory exists before serving with Docker
5. Check the antora-playbook.yml for site configuration settings