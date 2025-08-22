# Portals Permission Plugin

Minecraft Paper plugin for managing portal access permissions with knockback effects.

## Features

- **Portal Access Control**: Block access to Nether and End portals based on permissions
- **Smart Knockback**: Different knockback effects for different portal types
  - End portals: Upward and sideways knockback
  - Nether portals: Primarily sideways knockback
- **Cooldown System**: Configurable cooldown between portal access attempts
- **Multi-language Support**: English and Polish language support
- **Adventure Components**: Modern text formatting with MiniMessage support

## Requirements

- **Server**: Paper/Spigot 1.21.8+
- **Java**: Java 21+
- **Dependencies**: Adventure API (included in Paper)

## Installation

1. Download the latest JAR from [Releases](../../releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/PortalsPermission/config.yml`

## Permissions

- `portals.nether` - Access to Nether portals
- `portals.end` - Access to End portals
- `portals.custom` - Access to custom portals
- `portals.bypass` - Bypass all portal restrictions
- `portals.admin` - Administrative access
- `portals.reload` - Reload plugin configuration

## Configuration

The plugin creates a `config.yml` file with the following options:

```yaml
# Plugin settings
plugin:
  enabled: true
  debug: false
  language: "en"

# Portal blocking settings
portals:
  nether:
    block: true
  end:
    block: true
  custom:
    block: false

# Knockback settings
knockback:
  enabled: true
  force: 1.5
  height: 0.8
  sound: true

# Cooldown settings
cooldown:
  enabled: true
  time: 5
  show_message: true
```

## Commands

- `/portals reload` - Reload plugin configuration
- `/portals help` - Show help information

## Development

### Building

This project uses Maven for dependency management and building:

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Build JAR file
mvn clean package
```

### GitHub Actions

The project includes automated CI/CD with GitHub Actions:

- **Automatic Building**: Every push to main/master branch triggers a build
- **Automatic Releases**: Successful builds create GitHub releases with JAR files
- **Testing**: Runs unit tests before creating releases
- **Artifacts**: Uploads build artifacts for download

#### Workflow Features:

1. **Build Job**:
   - Sets up JDK 21
   - Caches Maven dependencies
   - Compiles and tests the code
   - Uploads JAR artifacts

2. **Release Job**:
   - Creates automatic releases for main/master branch pushes
   - Generates unique tags with timestamp and commit hash
   - Uploads both regular and shaded JAR files
   - Includes detailed release notes

3. **Notification Job**:
   - Reports build and release status
   - Provides feedback on workflow completion

#### Release Naming:

- **Tagged releases**: Uses the git tag (e.g., `v1.0.0`)
- **Branch releases**: Uses format `v{version}-{timestamp}-{commit}` (e.g., `v1.0-20241220-123456-abc1234`)

### Project Structure

```
src/main/java/pl/mikigal/portals/
├── PortalsPermission.java          # Main plugin class
├── commands/
│   └── PortalsCommand.java          # Command handler
├── events/
│   └── PortalAccessListener.java    # Portal event listener
├── managers/
│   ├── ConfigManager.java           # Configuration management
│   ├── CooldownManager.java         # Cooldown system
│   └── MessageManager.java          # Message handling
└── utils/
    └── PortalUtils.java             # Utility functions
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support, please create an issue on GitHub or contact the development team.