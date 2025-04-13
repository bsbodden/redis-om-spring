# Redis OM Spring Documentation

Documentation site for Redis OM Spring, based on [Antora](https://antora.org/).

## Building the Documentation

### Prerequisites

- Node.js 16+
- npm or yarn

### Installation

```bash
npm install
```

### Building the Site

```bash
npm run build
```

The documentation will be built in the `build/site` directory.

### Viewing the Documentation

After building, you can view the documentation by opening any of these files in your browser:

- `build/site/index.html` - Documentation homepage
- `build/site/redis-om-spring/current/index.html` - Current version documentation
- `build/site/redis-om-spring/current/overview.html` - Overview page

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

You can also serve the documentation using Docker:

```bash
docker-compose up
```

The documentation will be available at http://localhost:8000.

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