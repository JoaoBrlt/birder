#!/usr/bin/env python
import re
import csv
import json

def formatNameWithExtinct(name):
	"""Format a name and detect if the species is extinct."""
	extinct = False

	# Extinct species.
	if "†" in name:
		extinct = True
		name = name.replace("†", "")

	# Sanitize the name.
	name = name.strip()
	name = name.capitalize()

	# Name and extinct.
	return name, extinct

def formatName(name):
	"""Format a name."""
	name, extinct = formatNameWithExtinct(name)
	return name

def formatRegion(region):
	"""Format a region with abbreviations."""
	# The region is set.
	if (region):
		# All the region abbreviations.
		regions = {
			"NA": "North America",
			"MA": "Middle America",
			"SA": "South America",
			"LA": "Latin America",
			"AF": "Africa",
			"EU": "Eurasia",
			"OR": "Oriental Region",
			"AU": "Australasia",
			"AO": "Atlantic Ocean",
			"PO": "Pacific Ocean",
			"IO": "Indian Ocean",
			"TrO": "Tropical Ocean",
			"TO": "Temperate Ocean",
			"NO": "Nothern Ocean",
			"SO": "Southern Ocean",
			"AN": "Antartica",
			"NZ": "New Zealand"
		}
		# Replace all the region abbreviations.
		for i, j in regions.items():
			regex = re.compile(r'\b{}\b'.format(i))
			region = regex.sub(j, region)
		return region
	# The region is empty.
	return 'None'

def formatSubregion(subregion):
	"""Format a subregion with abbreviations."""
	# The subregion is set.
	if (subregion):
		# All the subregion abbreviations.
		subregions = {
			"nwc": "Northwest center",
			"nec": "Northeast center",
			"swc": "Southwest center",
			"sec": "Southeast center",
			"nc": "North center",
			"nw": "Northwest",
			"ne": "Northeast",
			"sc": "South center",
			"sw": "Southwest",
			"se": "Southeast",
			"wc": "West center",
			"ec": "East center",
			"c": "Center",
			"n": "North",
			"s": "South",
			"w": "West",
			"e": "East"
		}
		# Replace all the subregion abbreviations.
		for i, j in subregions.items():
			regex = re.compile(r'\b{}\b'.format(re.escape(i)))
			subregion = regex.sub(j, subregion)
		return subregion
	# The subregion is empty.
	return 'None'

def extractTranslations(row, list, languages=None):
	"""Extract languages and translations from three rows."""
	# Locations of the translations.
	locations = [
		[6 + 3 * i for i in range(10)], # First row.
		[4 + 3 * i for i in range(10)], # Second row.
		[5 + 3 * i for i in range(10)] # Third row.
	]

	# Translations object.
	translation = {}
	index = 0

	# For each translation line.
	for j in range(0, 3):
		# For each language of the translation line.
		for i in locations[j]:
			if languages:
				# Use languages as keys.
				translation[languages[index]] = row[i]
			else:
				# Use index as keys.
				translation[index] = row[i]

			# Next language.
			index += 1

		# Next row.
		if j < 2:
			row = next(list, None)

	# Translations object.
	return translation


def getTranslations():
	"""Get the name translations from a CSV file."""
	# Read the file.
	file = open("ioc-multilingual.csv", "r")
	list = csv.reader(file, delimiter=",", quotechar="\"")

	# Translations array.
	array = []

	# Get the languages.
	languages = extractTranslations(next(list), list)

	# For each row.
	for row in list:
		# Scientific name.
		if (row[3]):
			# Name.
			name = formatName(row[3])

			# Extract the translations.
			translations = extractTranslations(row, list, languages)
			array.append({
				"name": name,
				"translations": translations
			})

	# Close the file.
	file.close()

	# Translations array.
	return array

def findTranslation(translations, name):
	"""Find the name translations with the scientific name."""
	# For each translation object.
	for translation in translations:
		if translation["name"] == name:
			return translation["translations"]
	# No translation object found.
	return []

def getSpecies(translations):
	"""Get the species from a CSV file."""
	# Read the file.
	file = open("ioc.csv", "r")
	list = csv.reader(file, delimiter=",", quotechar="\"")

	# Species array.
	array = []

	# Temporary variables.
	taxon = ""
	order = ""
	family = ""
	genus = ""

	# Ignore headers.
	for i in range(0, 4):
		next(list)

	# For each row.
	for row in list:
		# Taxon.
		if (row[0]):
			# Name.
			taxon = formatName(row[0])

		# Order.
		elif (row[1]):
			# Name.
			order = formatName(row[1])

		# Family.
		elif (row[2]):
			# Name.
			family = formatName(row[2])

		# Genus.
		elif (row[4]):
			# Name.
			genus, extinct = formatNameWithExtinct(row[4])

		# Species.
		elif (row[5]):
			# Name and extinct.
			species, extinct = formatNameWithExtinct(row[5])
			name = genus + " " + species.lower()

			# Common names.
			commonNames = findTranslation(translations, name)

			# Ranges.
			breedingRegion = formatRegion(row[9])
			breedingSubregion = formatSubregion(formatRegion(row[10]))
			nonbreedingSubregion = formatSubregion(formatRegion(row[11]))

			array.append({
				"name": name,
				"taxon": taxon,
				"order": order,
				"family": family,
				"genus": genus,
				"species": species,
				"extinct": extinct,
				"commonNames": commonNames,
				"breedingRegion": breedingRegion,
				"breedingSubregion": breedingSubregion,
				"nonbreedingSubregion": nonbreedingSubregion
			})

	# Close the file.
	file.close()

	# Species array.
	return array

def saveCollection(species):
	"""Save the species in a json file."""
	file = open("species.json", "w", encoding="utf8")
	list = {
		"species": species
	}
	file.write(json.dumps(list, ensure_ascii=False))
	file.close()

def main():
	translations = getTranslations()
	species = getSpecies(translations)
	saveCollection(species)

main()
