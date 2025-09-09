# Contributing to Feign Gateway

Thank you for your interest in contributing to Feign Gateway! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Process](#development-process)
- [Code Style](#code-style)
- [Testing](#testing)
- [Documentation](#documentation)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/). By participating, you are expected to uphold this code.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/yourusername/feign-gateway.git
   cd feign-gateway
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/duyliken3/feign-gateway.git
   ```

### Development Setup

1. Build the project:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Run tests:
   ```bash
   mvn test
   ```

## Development Process

### Branch Strategy

- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: Feature development branches
- `bugfix/*`: Bug fix branches
- `hotfix/*`: Critical bug fixes

### Creating a Feature Branch

```bash
# Create and switch to a new feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b bugfix/your-bug-description
```

### Making Changes

1. Make your changes
2. Write or update tests
3. Update documentation if needed
4. Ensure all tests pass
5. Commit your changes with a clear message

### Commit Message Format

Use conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build process or auxiliary tool changes

Examples:
```
feat(gateway): add rate limiting support
fix(whitelist): resolve path matching issue
docs(api): update endpoint documentation
```

## Code Style

### Java Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods small and focused
- Use proper indentation (4 spaces)

### Code Formatting

The project uses Spotless for code formatting:

```bash
# Format code
mvn spotless:apply

# Check formatting
mvn spotless:check
```

### Import Organization

- Use static imports sparingly
- Group imports logically
- Remove unused imports

## Testing

### Test Requirements

- All new features must have unit tests
- Integration tests for API endpoints
- Test coverage should be maintained above 80%
- Write tests before implementing features (TDD)

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=WhitelistServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Guidelines

- Use descriptive test method names
- Follow AAA pattern (Arrange, Act, Assert)
- Mock external dependencies
- Test both positive and negative cases
- Keep tests independent and isolated

## Documentation

### Documentation Requirements

- Update README.md for significant changes
- Add Javadoc for public APIs
- Update API documentation for endpoint changes
- Update architecture docs for structural changes
- Add examples for new features

### Documentation Types

- **API Documentation**: Update `API_DOCUMENTATION.md`
- **Architecture**: Update `ARCHITECTURE.md`
- **Deployment**: Update `DEPLOYMENT_GUIDE.md`
- **Testing**: Update `TESTING_GUIDE.md`

## Pull Request Process

### Before Submitting

1. Ensure all tests pass
2. Update documentation
3. Add or update tests
4. Follow code style guidelines
5. Update CHANGELOG.md

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
```

### Review Process

1. Automated checks must pass
2. At least one reviewer approval required
3. Address all review comments
4. Squash commits if requested
5. Merge after approval

## Issue Guidelines

### Creating Issues

- Use clear, descriptive titles
- Provide detailed descriptions
- Include steps to reproduce (for bugs)
- Add labels appropriately
- Reference related issues/PRs

### Issue Types

- **Bug**: Something isn't working
- **Enhancement**: New feature or improvement
- **Documentation**: Documentation improvements
- **Question**: Questions or help requests

### Bug Reports

Include:
- Clear description
- Steps to reproduce
- Expected vs actual behavior
- Environment details
- Screenshots/logs if applicable

## Release Process

### Version Numbering

Follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Incompatible API changes
- **MINOR**: New functionality (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

1. Update version in `pom.xml`
2. Update `CHANGELOG.md`
3. Create release notes
4. Tag the release
5. Deploy to production

## Getting Help

- Check existing issues and discussions
- Ask questions in GitHub Discussions
- Contact maintainers directly
- Join our community chat (if available)

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project documentation

Thank you for contributing to Feign Gateway! 🚀
