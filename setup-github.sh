#!/bin/bash

# Setup script for creating ProjectGeminiApps repository on GitHub
# This script helps create the repository and push the code

echo "🚀 GitHub Repository Setup for ProjectGeminiApps"
echo ""

# Check if gh CLI is available
if command -v gh &> /dev/null; then
    echo "Using GitHub CLI (gh) to create repository..."
    gh repo create ProjectGeminiApps --public --source=. --remote=origin --description="KMM WebView white-label app framework with product flavors"

    if [ $? -eq 0 ]; then
        echo "✅ Repository created successfully!"

        # Push master and dev branches
        echo "📤 Pushing master branch..."
        git push -u origin master

        echo "📤 Pushing dev branch..."
        git push -u origin dev

        echo "✅ All done! Repository URL:"
        gh repo view --web
    else
        echo "❌ Failed to create repository with gh CLI"
        exit 1
    fi
else
    echo "GitHub CLI (gh) not found. Please install it or create the repository manually."
    echo ""
    echo "📋 Manual steps:"
    echo "1. Go to https://github.com/new"
    echo "2. Repository name: ProjectGeminiApps"
    echo "3. Description: KMM WebView white-label app framework with product flavors"
    echo "4. Choose Public or Private"
    echo "5. Don't initialize with README (we already have code)"
    echo "6. Click 'Create repository'"
    echo ""
    echo "Then run these commands:"
    echo ""
    read -p "Enter your GitHub username: " github_username

    if [ -z "$github_username" ]; then
        echo "❌ GitHub username is required"
        exit 1
    fi

    echo "🔗 Adding remote origin..."
    git remote add origin "https://github.com/$github_username/ProjectGeminiApps.git" || git remote set-url origin "https://github.com/$github_username/ProjectGeminiApps.git"

    echo "📤 Pushing master branch..."
    git push -u origin master

    if [ $? -eq 0 ]; then
        echo "📤 Pushing dev branch..."
        git push -u origin dev

        echo "✅ All done! Repository URL:"
        echo "https://github.com/$github_username/ProjectGeminiApps"
    else
        echo "❌ Failed to push. Make sure the repository exists on GitHub."
        echo "Visit: https://github.com/$github_username/ProjectGeminiApps"
    fi
fi
