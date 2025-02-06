//
//  BusSearchView.swift
//  iosApp
//
//  Created by Vishal Gupta on 27/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import MapKit




let busStops = [
    "Central Station",
    "Main Street",
    "Park Avenue",
    "North Campus",
    "South Campus",
    "City Hall",
    "East End Terminal",
    "West Gate",
    "Library",
    "Museum Square",
    "Tech Park",
    "Riverfront",
    "University Entrance",
    "Market Street",
    "Sports Complex"
]

struct TextFieldEx2: View {
    
    @State private var text: String = ""

        var body: some View {
            HStack {
                TextField("Enter text", text: $text)
                    .padding(18)
                    .background(Color.matteBackground)
                    .cornerRadius(23)
            }
            .padding(.horizontal,10)
            SourceDestinationList()
        }
}

struct SourceDestinationList: View {
    var body: some View {
        
        ScrollView {
            ForEach(busStops, id: \.self) { stop in
                Text(stop).frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.vertical,2)
                    .padding(.horizontal)
                Divider()
            }
        }//.padding(40)
        .background(Color.matteBackground)
        .cornerRadius(23)
        .padding(10)
    }
}
struct TextFieldEx: View {
    
    @State private var text: String = ""

        var body: some View {
            HStack {
                TextField("Enter text", text: $text)
                    .padding(18)
                    .background(Color.matteBackground)
                    .background(.gray)
                    .cornerRadius(23)
                    .overlay(
                        HStack {
                            Spacer()
                            Button(action: {
                                // Action for button
                                print("Button pressed")
                            }) {
                                Text("Go")
                                .foregroundColor(.white)
                                   
                            }
                              
                                //.frame(width: 50,height: 49.5)
//                                .padding(.horizontal, 10)
//                                .padding(.vertical, 5)
                                .padding()
                                .background(Color.customPrimary)
                                .cornerRadius(23)
                                .padding(.trailing, 2) // Adds padding between button and text field edge
                        }
                    )
            }
            .padding(.horizontal,10)
        }
}




// Modified BusCardView to accept parameters
extension Color {
    static let customPrimary = Color(hex: "#301E3F")
    static let customSecondary = Color(hex: "#5D4A8E")
    static let customAccent = Color(hex: "#8B6BBE")
    static let matteBackground = Color(hex: "#E6E1E6")
    
    // Darker shades
    static let customPrimaryDark1 = Color(hex: "#24162E")
    static let customPrimaryDark2 = Color(hex: "#1A1022")
    static let customPrimaryDark3 = Color(hex: "#120B18")
}

// Hex to Color initializer
extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        _ = scanner.scanString("#") // Skip the # character
        var rgb: UInt64 = 0
        scanner.scanHexInt64(&rgb)

        let red = Double((rgb >> 16) & 0xFF) / 255.0
        let green = Double((rgb >> 8) & 0xFF) / 255.0
        let blue = Double(rgb & 0xFF) / 255.0

        self.init(red: red, green: green, blue: blue)
    }
}

// Main view that holds a list of BusCardViews
struct BusCardListView: View {
    let buses = [
        ("Bus 101", "John Doe", "23:00 - 21:00", "Mon - Fri", ["Park Avenue Street", "Riverside Street"]),
        ("Bus 102", "Jane Smith", "06:00 - 18:00", "Mon - Fri", ["Main Street", "Second Avenue"]),
        ("Bus 103", "Alice Johnson", "07:00 - 19:00", "Mon - Fri", ["First Street", "Third Avenue"]),
        ("Bus 104", "Bob Brown", "05:00 - 20:00", "Mon - Fri", ["Fourth Street", "Fifth Avenue","First Street","First Street","First Street","First Street","First Street","First Street","First Street","First Street","First Street","First Street","First Street","First Street"])
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                ForEach(buses, id: \.0) { bus in
                    BusCardView(busID: bus.0, driverName: bus.1, activeHours: bus.2, activeDays: bus.3, busStops: bus.4)
                }
            }
            .padding()
        }
    }
}

// Modified BusCardView to accept parameters
struct BusCardView: View {
    let busID: String
    let driverName: String
    let activeHours: String
    let activeDays: String
    let busStops: [String]
    
    @State private var isActive: Bool = true
    @State private var isSheetPresented = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text(isActive ? "Active" : "Not In Service")
                    .font(.subheadline)
                    .bold()
                    .foregroundColor(isActive ? .green : .red)
                
                Circle()
                    .fill(isActive ? Color.green : Color.red)
                    .frame(width: 10, height: 10)
                
                Text(busID)
                    .font(.headline)
                    .foregroundColor(.customPrimary)
                
                Spacer()
                
                Text(driverName)
                    .font(.subheadline)
                    .foregroundColor(.customSecondary)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text("Active Hours: \(activeHours)")
                    .font(.subheadline)
                    .foregroundColor(.customAccent)
                
                Text("Active Days: \(activeDays)")
                    .font(.subheadline)
                    .foregroundColor(.customAccent)
            }

            HStack {
                Button("Stops") {
                    isSheetPresented = true
                }
                .buttonStyle(PrimaryButtonStyle())
                
                Spacer()
                
                Button("Track") {
                    // Action for Track button
                }
                .buttonStyle(PrimaryButtonStyle())
                
                Spacer()
                
                Button("Navigate Bus Stop") {
                    // Action for Navigate button
                }
                .buttonStyle(PrimaryButtonStyle())
            }
        }
        .padding()
        .background(Color.matteBackground) // Use matte background color
        .cornerRadius(23)
        .shadow(radius: 4)
        .sheet(isPresented: $isSheetPresented) {
            BusStopsSheetView(busStops: busStops)
        }
    }
}

// View for the sheet that lists bus stops
struct BusStopsSheetView: View {
    let busStops: [String]
    
    var body: some View {
        ZStack {
            Color.customPrimaryDark1.opacity(0.9)
                .ignoresSafeArea()
            
            VStack(alignment: .center, spacing: 16) {
                    Text("Bus Stops")
                        .font(.title2)
                        .bold()
                        .foregroundColor(.customAccent)
                        .padding(.bottom, 8)
                    
                    ScrollView{
                        ForEach(busStops, id: \.self) { stop in
                            BusStopCard(stop: stop)
                        }
                        
                    }
                    
                    Spacer()
                }.padding()
        }
    }
}

struct TextFieldDropDownCard: View {
    let stop: String
    
    var body: some View {
        HStack {
            Text(stop)
                .font(.body)
                .foregroundColor(.customSecondary)

            Spacer()
            
            Button("Navigate") {
                // Navigation action
            }.buttonStyle(PrimaryButtonStyle())
        }
        .padding()
        .background(Color.matteBackground) // Matte background for the card
        .cornerRadius(23)
        .shadow(radius: 4)
    }
}

struct BusStopCard: View {
    let stop: String
    
    var body: some View {
        HStack {
            Text(stop)
                .font(.title3)
                .foregroundColor(.customSecondary)

            Spacer()
            
            Button("Navigate") {
                // Navigation action
            }.buttonStyle(PrimaryButtonStyle())
        }
        .padding()
        .background(Color.matteBackground) // Matte background for the card
        .cornerRadius(23)
        .shadow(radius: 4)
    }
}

// Custom button style for uniform styling
struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.vertical, 8)
            .padding(.horizontal,12)
            .background(Color.customSecondary) // Use custom secondary color for
            .foregroundColor(.white)
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

struct BusRouteSelectionView: View {
    @State private var selectedButton: String? // Track which button is selected
    @State var isClicked : () -> Void = {}
   
    var body: some View {
        VStack
        {        HStack {
            // Bus ID Button
            Button(action: {
                isClicked()
                selectedButton = "BusID"
               
                
            }) {
                Text("Bus ID")
                    .padding()
                    .frame(maxWidth: .infinity) // Take full width
                    .background(selectedButton == "BusID" ? Color.customSecondary : Color.customPrimary)
                    .clipShape(
                        .rect(
                            topLeadingRadius: 0,
                            bottomLeadingRadius: 20,
                            bottomTrailingRadius: 0,
                            topTrailingRadius: 20
                        )
                    )
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            
            // Route Button
            Button(action: {
                selectedButton = "Route"
                isClicked()
            }) {
                Text("Route")
                    .padding()
                    .frame(maxWidth: .infinity) // Take full width
                    .background(selectedButton == "Route" ? Color.customSecondary : Color.customPrimary)
                    .background(selectedButton == "BusID" ? Color.customSecondary : Color.customPrimary)
                    .clipShape(
                        .rect(
                            topLeadingRadius: 0,
                            bottomLeadingRadius: 20,
                            bottomTrailingRadius: 0,
                            topTrailingRadius: 20
                        )
                    )
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
        .padding()
        .frame(maxWidth: .infinity) // Ensure the HStack takes full width of the screen
            
           
            
        }
    }
}

struct Test : View {
    @State private var isByRoute: Bool = false

    var body: some View {

        VStack{
                VStack(){
                    BusRouteSelectionView(){
                        withAnimation {
                        isByRoute.toggle() // Use withAnimation for smooth transition
            }
                    }
                    
                    if !isByRoute {
                        withAnimation {
                            TextFieldEx2()
                        }
                       
                    }
                   
                    TextFieldEx()
                }
                .padding(.bottom,10)
                
           
           
         
            BusCardListView()
           // BusCardView()
            
        }.padding(.horizontal,5)
            .background(Color.customPrimaryDark1)
    }
}


struct ETAAndDistanceCardView: View {
    @State private var showDetails: Bool = false // State variable to control visibility of current and next stop
    @State var isClicked : Bool = false
    
    let eta: String // Estimated Time of Arrival
    let distance: String // Distance to next stop
    let currentStop: String // Current bus stop
    let nextStop: String // Next bus stop
    
    var body: some View {
        ZStack {
            // Full-screen Map
            Map()
                .edgesIgnoringSafeArea(.all) // Make map fill the entire screen
            
            // Card header with ETA and Distance
            VStack {
                Spacer()
                
                   VStack {
                       Row1(){
                           withAnimation(.easeInOut){
                               isClicked.toggle()
                           }
                          
                       }
                    if isClicked{
                        Row2()
                    }
                 
                }
                .padding(15) // Padding around the HStack
                .background(Color.customPrimaryDark1)
                .cornerRadius(23) // Rounded corners
                .shadow(color: Color.black.opacity(0.8), radius: 5, x: 0, y: 2)
    
            }
            .padding(.horizontal, 16)
            .padding(.bottom,20)
        }
    }
}


struct Row1: View {
    @State var isClicked : () -> Void =  {}
    
    @State var isClicked2 : Bool  = false

    var body: some View {
        HStack{
            
            Button(action: {
                isClicked()
                    isClicked2.toggle()
              
            }) {
                Image(systemName: isClicked2 ? "arrow.backward" : "arrow.backward")
                    .foregroundColor(Color.matteBackground)
                    .font(.title2)
                    
            }.padding(.trailing,5)
            
            VStack(alignment: .leading) {
                Text("ETA")
                    .font(.subheadline)
                    .foregroundColor(Color.matteBackground)
                Text("5 mins")
                    .font(.title)
                    .foregroundColor(Color.matteBackground)
            }
            
            Spacer()
            
            VStack(alignment: .trailing) {
                Text("Distance")
                    .font(.subheadline)
                    .foregroundColor(Color.matteBackground)
                Text("2 km")
                    .font(.title)
                    .foregroundColor(Color.matteBackground)
            }
            
            // Button with Down Arrow
            Button(action: {
                isClicked()
                    isClicked2.toggle()
              
            }) {
                Image(systemName: isClicked2 ? "chevron.up" : "chevron.down")
                    .foregroundColor(Color.matteBackground)
                    .font(.title2)
                    
            }.padding(.leading,5)
        }.padding(5)
       
    }
}


struct Row2: View {
    var body: some View {
        VStack(alignment: .center) {
            // First card
            VStack(alignment: .center) {
                Text("Current Stop")
                    .frame(maxWidth: .infinity,alignment: .leading)
                    .font(.headline) // Smaller font size for better readability
                    .foregroundColor(Color.customPrimary)
                    .padding(.horizontal, 10)
                
                Text("South Avenue Street")
                    .frame(maxWidth: .infinity,alignment: .leading)
                    .font(.body) // Smaller font for content
                    .foregroundColor(Color.customPrimary)
                    .padding(.horizontal, 10)
            }
            .padding(5) // Padding around the card content
            .frame(maxWidth: .infinity) // Fill maximum width
            .background(Color.matteBackground)
            .cornerRadius(23)
            .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2) // Add a subtle shadow

            // Second card
            VStack(alignment: .center) {
                Text("Next Stop")
                  
                    .font(.headline) // Smaller font size for consistency
                    .foregroundColor(Color.customPrimary)
                    .padding(.horizontal, 10)
                    .frame(maxWidth: .infinity,alignment: .leading)

                Text("Park Avenue North South Street")
                    .frame(maxWidth: .infinity,alignment: .leading)
                    .font(.body) // Smaller font for content
                    .foregroundColor(Color.customPrimary)
                    .padding(.horizontal, 10)
            }
            .padding(5) // Padding around the card content
            .frame(maxWidth: .infinity) // Fill maximum width
            .background(Color.matteBackground)
            .cornerRadius(23)
            .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2) // Add a
            
        }.frame(maxWidth: .infinity) // Ensure outer VStack expands to maximum width
            .transition(.move(edge: .bottom)) // Apply slide-up transition
            .animation(.easeInOut)
    }
}

struct Test2 : View {
    var body: some View {
        ETAAndDistanceCardView(eta: "5 mins", distance: "2 km", currentStop: "Park Avenue", nextStop: "Riverside Street")
                  
    }
}


struct ChooseOptionView_Previews: PreviewProvider {
    static var previews: some View {
        //ChooseOptionView()
        //TextFieldEx()
        //DropdownTextField()
       Test()
       
    }
}
//struct BusSearchViewPreview: PreviewProvider {
//    static var previews: some View{
//        BusSearchView()
//    }
//}


//
//struct BusSearchView: View {
//    @State var text1 = ""
//    var body: some View {
//        NavigationStack{
//            HStack(alignment: .top){
//                VStack(){
//                    TextField("From Destination", text: $text1)
//                        .padding(10)
//                        .background(Color.white)
//                        .cornerRadius(20)
//                        .foregroundColor(.blue)
//                        .font(.system(size: 23, weight: .medium))
//                        .overlay(RoundedRectangle(cornerRadius: 20)
//                            .stroke(Color.blue, lineWidth: 3))
//                        .shadow(color: Color.gray.opacity(0.5), radius: 4, x: 0, y: 2)
//                        .keyboardType(.default)
//                        .autocorrectionDisabled(true)
//                        .textInputAutocapitalization(.words)
//                        .navigationBarTitleDisplayMode(.inline)
//                        .navigationBarTitleDisplayMode(.inline) // Optional: use inline mode if desired
//                        .toolbar {
//                            ToolbarItem(placement: .principal) { // Center the title
//                                Text("Track My Shuttle")
//                                    .font(.system(size: 24, weight: .bold)) // Customize the font size and weight here
//                                    .foregroundColor(.blue) // Customize the color if needed
//                            }
//                        }
//                    
//                    Divider()
//                    TextField("From Destination", text: $text1)
//                        .padding(10)
//                        .background(Color.white)
//                        .cornerRadius(20)
//                        .foregroundColor(.blue)
//                        .font(.system(size: 23, weight: .medium))
//                        .overlay(RoundedRectangle(cornerRadius: 20)
//                            .stroke(Color.blue, lineWidth: 3))
//                        .shadow(color: Color.gray.opacity(0.5), radius: 4, x: 0, y: 2)
//                        .keyboardType(.default)
//                        .autocorrectionDisabled(true)
//                        .textInputAutocapitalization(.words)
//                    
//                    Button(action: {} , label: {
//                        Text("Search")
//                            .font(.system(size: 23, weight: .medium))
//                    })
//                    .padding(10)
//                    .background(Color.blue)
//                    .foregroundColor(Color.white)
//                    .cornerRadius(20)
//                    
//                    //Spacer()
//                    
//                }.padding(10)
//                    .frame( height: 200,alignment: .top)
//                    .background(
//                        LinearGradient(
//                            gradient: Gradient(colors: [
//                                Color(red: 48/255, green: 25/255, blue: 52/255), // Dark violet color
//                                Color(red: 138/255, green: 43/255, blue: 226/255) // Lighter violet
//                            ]),
//                            startPoint: .top,
//                            endPoint: .bottom
//                        )
//                    )
//                
//            }
//        }
//    }
//}


//struct DropdownTextField: View {
//    @State private var text: String = ""
//    @State private var isDropdownVisible: Bool = false
//
//    // Sample data for dropdown suggestions
//    let suggestions = ["Apple", "Banana", "Orange", "Pineapple", "Grapes", "Mango", "Strawberry"]
//
//    var body: some View {
//        VStack {
//            TextField("Search...", text: $text, onEditingChanged: { isEditing in
//                self.isDropdownVisible = isEditing
//            })
//            .padding()
//            .background(Color(.systemGray6))
//            .cornerRadius(8)
//            .overlay(
//                RoundedRectangle(cornerRadius: 8)
//                    .stroke(Color.gray, lineWidth: 1)
//            )
//            .onChange(of: text) { _ in
//                // Show dropdown if text is not empty
//                self.isDropdownVisible = !text.isEmpty
//            }
//
//            // Dropdown suggestions
//            if isDropdownVisible {
//                ScrollView {
//                    VStack(alignment: .leading, spacing: 0) {
//                        ForEach(filteredSuggestions, id: \.self) { suggestion in
//                            Text(suggestion)
//                                .padding()
//                                .frame(maxWidth: .infinity, alignment: .leading)
//                                .background(Color.white)
//                                .onTapGesture {
//                                    self.text = suggestion
//                                    self.isDropdownVisible = false
//                                }
//                                .foregroundColor(.black)
//                        }
//                    }
//                    .background(Color.white)
//                    .cornerRadius(8)
//                    .shadow(radius: 5)
//                }
//                .frame(maxHeight: 150) // Limit the height of dropdown
//            }
//            TextFieldEx()
//        }
//        .padding()
//    }
//
//    // Filtered list based on user input
//    var filteredSuggestions: [String] {
//        suggestions.filter { $0.lowercased().contains(text.lowercased()) }
//    }
//}

//import SwiftUI
//
//struct ChooseOptionView: View {
//    @State private var busId: String = "" // State for Bus ID input
//    @State private var source: String = "" // State for Source input
//    @State private var destination: String = "" // State for Destination input
//    
//    var body: some View {
//        ZStack {
//            // Background gradient
//            LinearGradient(
//                gradient: Gradient(colors: [
//                    Color(red: 48/255, green: 25/255, blue: 52/255), // Dark violet
//                    Color(red: 138/255, green: 43/255, blue: 226/255) // Lighter violet
//                ]),
//                startPoint: .top,
//                endPoint: .bottom
//            )
//            .edgesIgnoringSafeArea(.all) // Extend gradient to cover entire screen
//            
//            VStack(spacing: 20) {
//                Text("Track Your Shuttle")
//                    .font(.largeTitle)
//                    .foregroundColor(.white)
//                    .padding(.top, 40) // Space from the top
//                
//                // Text field for Bus ID
//                TextField("Enter Bus ID", text: $busId)
//                    .padding()
//                    .background(Color.white.opacity(0.8))
//                    .cornerRadius(10)
//                    .padding(.horizontal)
//                
//                // Divider with "OR" text
//                HStack {
//                    Divider()
//                    Text("OR")
//                        .font(.headline)
//                        .foregroundColor(.white)
//                        .padding(.horizontal)
//                    Divider()
//                }
//                .padding(.vertical, 10) // Add vertical padding
//                
//                // Text fields for Source and Destination
//                VStack(spacing: 10) {
//                    TextField("Enter Source", text: $source)
//                        .padding()
//                        .background(Color.white.opacity(0.8))
//                        .cornerRadius(10)
//                        .padding(.horizontal)
//                    
//                    TextField("Enter Destination", text: $destination)
//                        .padding()
//                        .background(Color.white.opacity(0.8))
//                        .cornerRadius(10)
//                        .padding(.horizontal)
//                }
//                
//                Spacer() // Pushes content to the top
//            }
//            .padding() // Add padding around the VStack
//        }
//    }
//}
//
